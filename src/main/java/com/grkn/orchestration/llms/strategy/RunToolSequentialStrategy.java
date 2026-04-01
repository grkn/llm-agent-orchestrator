package com.grkn.orchestration.llms.strategy;

import com.grkn.orchestration.llms.core.DefaultToolRunnerImpl;
import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.dto.CallableResponse;
import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.fsm.Message;
import com.grkn.orchestration.llms.interfaces.ToolRunner;
import tools.jackson.core.JsonParser;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.core.util.JacksonFeature;
import tools.jackson.core.util.JacksonFeatureSet;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * Strategy for executing tools sequentially.
 */
public class RunToolSequentialStrategy implements ActionStrategy {
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
        List<String> toolNames = apiResponse.getToolNames();
        List<Map<String, String>> inputs = apiResponse.getInputs();

        List<CallableResponse> results = toolRunner.run(Action.RUN_TOOL_SEQUENTIAL, toolNames, inputs ,abstractAgent);

        CallableResponse response = results.getFirst();

        if (response.isSuccess()) {
            message.getPayload().setToolOutput(objectMapper.writeValueAsString(response.getResponse()));
        } else {
            message.getPayload().setToolOutput(objectMapper.writeValueAsString(response.getError()));
        }


        return message;
    }
}
