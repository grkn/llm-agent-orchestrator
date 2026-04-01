package com.grkn.orchestration.llms.core;

import com.grkn.orchestration.llms.interfaces.ToolFinder;
import com.grkn.tool.library.annotation.Tool;

import java.lang.reflect.Method;
import java.util.List;

public class DefaultToolFinder implements ToolFinder {
    private final List<Method> toolMethods;

    public DefaultToolFinder(List<Method> toolMethods) {
        this.toolMethods = toolMethods;
    }

    @Override
    public Method find(String toolName) {
        return toolMethods.stream()
                .filter(m -> m.getDeclaredAnnotation(Tool.class) != null)
                .filter(m -> m.getDeclaredAnnotation(Tool.class).name().equals(toolName))
                .findFirst()
                .orElse(null);
    }
}
