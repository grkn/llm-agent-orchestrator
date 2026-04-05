package com.grkn.orchestration.llms.core;

import com.grkn.orchestration.llms.dto.CallableResponse;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.interfaces.ToolFinder;
import com.grkn.tool.library.annotation.ToolParameter;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Callable;

public class LlmCallable implements Callable<CallableResponse> {
    private final String toolName;
    private final Object input;
    private final ToolFinder toolFinder ;
    private final Object toolInstance;
    private static final ObjectMapper objectMapper;

    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class, String.class
    );

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
    public CallableResponse call() {
        Method toolMethod = toolFinder.find(toolName);

        if(toolMethod == null) {
            return new CallableResponse(null, false, toolName, "Tool not found");
        }

        Parameter instanceClassOfInput = Arrays.stream(toolMethod.getParameters()).filter(parameter ->
                parameter.getDeclaredAnnotation(ToolParameter.class) != null).findFirst().orElseThrow();
        Class<?> type = instanceClassOfInput.getType();

        Object instance;
        if(WRAPPER_TYPES.contains(type) || type.isPrimitive()) {
            instance = ((LinkedHashMap<?, ?>) input).get(instanceClassOfInput.getName());
        } else {
            // Linked hashmap is converted to string then converted to instance
            instance = objectMapper.readValue(objectMapper.writeValueAsString(input), type);
        }
        // Execution of tool method
        try {
            Object result = toolMethod.invoke(toolInstance, instance);
            return new CallableResponse(result, true, toolName, null);
        } catch (Exception e) {
            return new CallableResponse(null, false, toolName, "Tool execution failed: " + e.getMessage());
        }
    }
}
