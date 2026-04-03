package com.grkn.orchestration.llms.strategy;

import com.grkn.orchestration.llms.core.DefaultToolRunnerImpl;
import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.dto.CallableResponse;
import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.fsm.Message;
import com.grkn.orchestration.llms.interfaces.ToolRunner;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Strategy for executing tools in parallel.
 */
public class RunToolParallelStrategy implements ActionStrategy {
    private final ToolRunner<List<CallableResponse>> toolRunner = DefaultToolRunnerImpl.INSTANCE;
    private static final ObjectMapper objectMapper;

    static {
        JsonMapper.Builder builder = JsonMapper.builder();
        builder.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES);
        builder.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        objectMapper = builder.build();
    }

    @Override
    public Message execute(Message message, ApiResponse apiResponse, AbstractAgent abstractAgent) throws Exception {
        if (abstractAgent.getToolClassInstance() == null) {
            ApiResponse response = new ApiResponse();
            response.setAction(Action.ASK_AGENT.name());
            response.setAgentName(abstractAgent.getName());
            response.setAnswer("I don't have any responsibility to use tools. Skipping the task");
            message.setPayload(response);
            return message;
        }

        List<String> toolNames = apiResponse.getToolNames();
        List<Map<String,String>> inputs = apiResponse.getInputs();

        List<CallableResponse> result = toolRunner.run(Action.RUN_TOOL_PARALLEL, toolNames, inputs, abstractAgent);
        List<Object> actualResults = new ArrayList<>();
        for (CallableResponse callableResponse : result) {
            if (callableResponse.isSuccess()) {
                actualResults.add(callableResponse.getResponse());
            } else {
                actualResults.add(callableResponse.getError());
            }
        }
        message.getPayload().setToolOutput(objectMapper.writeValueAsString(actualResults));
        return message;
    }
}
