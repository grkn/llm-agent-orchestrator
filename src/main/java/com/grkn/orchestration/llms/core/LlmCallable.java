package com.grkn.orchestration.llms.core;

import com.grkn.orchestration.llms.dto.CallableResponse;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.interfaces.ToolFinder;
import com.grkn.tool.library.annotation.ToolParameter;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class LlmCallable implements Callable<CallableResponse> {
    private final String toolName;
    private final Object input;
    private final ToolFinder toolFinder ;
    private final Object toolInstance;
    private static final ObjectMapper objectMapper;

    static {
        JsonMapper.Builder builder = JsonMapper.builder();
        for (JsonReadFeature value : JsonReadFeature.values()) {
            builder.enable(value);
        }
        objectMapper = builder.build();
    }

    public LlmCallable(String toolName, Object input, AbstractAgent agent) {
        this.toolName = toolName;
        this.input = input;
        this.toolFinder = new DefaultToolFinder(agent.getToolMethods());
        this.toolInstance = agent.getToolClassInstance();
    }

    @Override
    public CallableResponse call() throws Exception {
        Method toolMethod = toolFinder.find(toolName);

        if(toolMethod == null) {
            return new CallableResponse(null, false, toolName, "Tool not found");
        }

        Class<?> instanceClassOfInput = Arrays.stream(toolMethod.getParameters()).filter(parameter ->
                parameter.getDeclaredAnnotation(ToolParameter.class) != null).findFirst().orElseThrow().getType();
        // Linked hashmap is converted to string then converted to instance
        Object instance = objectMapper.readValue(objectMapper.writeValueAsString(input), instanceClassOfInput);
        // Execution of tool method
        Object result = toolMethod.invoke(toolInstance, instance);

        return new CallableResponse(result, true, toolName, null);
    }
}
