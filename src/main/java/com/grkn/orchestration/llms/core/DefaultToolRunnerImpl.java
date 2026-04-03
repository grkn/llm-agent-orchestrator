package com.grkn.orchestration.llms.core;

import com.grkn.orchestration.llms.dto.CallableResponse;
import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.interfaces.ToolRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DefaultToolRunnerImpl implements ToolRunner<List<CallableResponse>> {

    public final static ToolRunner<List<CallableResponse>> INSTANCE = new DefaultToolRunnerImpl();
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);

    @Override
    public List<CallableResponse> run(Action action, List<String> toolNames, List<Map<String, String>> inputs, AbstractAgent agent) throws InterruptedException {
        if (toolNames.size() != inputs.size()) {
            return List.of(new CallableResponse(null, false, "ToolRunner", "Tool names array and inputs array should be equal size"));
        }
        return switch (action) {
            case RUN_TOOL_PARALLEL -> runParallel(toolNames, inputs, agent);
            case RUN_TOOL_SEQUENTIAL -> runSequential(toolNames, inputs, agent);
            default -> new ArrayList<>();
        };
    }

    private List<CallableResponse> runSequential(List<String> toolNames, List<Map<String, String>> inputs, AbstractAgent agent) {
        List<CallableResponse> responses = new ArrayList<>();
        for (int i = 0; i < toolNames.size(); i++) {
            LlmCallable callable = new LlmCallable(toolNames.get(i), inputs.get(i), agent);
            responses.add(callable.call());
        }
        return responses;
    }

    private List<CallableResponse> runParallel(List<String> toolNames, List<Map<String, String>> inputs, AbstractAgent agent)
            throws InterruptedException {
        Collection<LlmCallable> callables = createCallables(toolNames, inputs, agent);
        List<Future<CallableResponse>> futures = executorService.invokeAll(callables);
        return getResponses(toolNames, futures);
    }

    private static List<CallableResponse> getResponses(List<String> toolNames, List<Future<CallableResponse>> futures) {
        List<CallableResponse> result = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            result.add(get(futures.get(i), toolNames.get(i)));
        }
        return result;
    }

    private static Collection<LlmCallable> createCallables(List<String> toolNames, List<Map<String, String>> inputs, AbstractAgent agent) {
        Collection<LlmCallable> callables = new ArrayList<>();

        for (int i = 0; i < toolNames.size(); i++) {
            String toolName = toolNames.get(i);
            Object input = inputs.get(i);
            callables.add(new LlmCallable(toolName, input, agent));
        }
        return callables;
    }

    private static CallableResponse get(Future<CallableResponse> future, String toolName) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return new CallableResponse(null, false, toolName,
                    "Error: " + e.getMessage() + " for tool: " + toolName);
        }
    }
}
