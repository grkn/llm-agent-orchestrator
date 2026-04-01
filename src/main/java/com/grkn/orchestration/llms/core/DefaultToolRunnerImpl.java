package com.grkn.orchestration.llms.core;

import com.grkn.orchestration.llms.dto.CallableResponse;
import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.interfaces.ToolRunner;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
    private static final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);

    static {
        JsonMapper.Builder builder = JsonMapper.builder();
        for (JsonReadFeature value : JsonReadFeature.values()) {
            builder.enable(value);
        }
        objectMapper = builder.build();
    }

    @Override
    public List<CallableResponse> run(Action action, List<String> toolNames,  List<Map<String,String>> inputs, AbstractAgent agent) throws InterruptedException {
        return switch (action) {
            case RUN_TOOL_PARALLEL -> runParallel(toolNames, inputs, agent);
            case RUN_TOOL_SEQUENTIAL -> runSequential(toolNames, inputs, agent);
            default -> new ArrayList<>();
        };
    }

    private List<CallableResponse> runSequential(List<String> toolNames, List<Map<String,String>> inputs, AbstractAgent agent) {
        LlmCallable callable = new LlmCallable(toolNames.getFirst(), inputs.getFirst(), agent);
        Future<CallableResponse> future = executorService.submit(callable);
        CallableResponse previous = getCallableResponseChain(toolNames, agent, future);
        return List.of(previous);
    }

    private CallableResponse getCallableResponseChain(List<String> toolNames, AbstractAgent agent, Future<CallableResponse> future) {
        LlmCallable callable;
        CallableResponse previous = get(future, toolNames.getFirst());
        for (int i = 1; i < toolNames.size(); i++) {
            callable = new LlmCallable(toolNames.get(i), previous.getResponse(), agent);
            future = executorService.submit(callable);
            previous = get(future, toolNames.get(i));
        }
        return previous;
    }

    private List<CallableResponse> runParallel(List<String> toolNames, List<Map<String,String>> inputs, AbstractAgent agent)
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

    private static Collection<LlmCallable> createCallables(List<String> toolNames, List<Map<String,String>> inputs, AbstractAgent agent) {
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
