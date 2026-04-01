package com.grkn.orchestration.llms.fsm;

import com.grkn.tool.library.core.Instances;
import com.grkn.tool.library.utility.ReflectionUtility;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Abstract base class for agents with default implementations.
 * Extend this class to create custom agents with specific behaviors.
 */
public abstract class AbstractAgent implements Agent {
    private final String name;
    protected Object toolClassInstance;
    protected final String prompt;
    protected List<Method> toolMethods;
    protected String toolDescription;

    protected AbstractAgent(String name, Object toolClassInstance, String prompt) {
        if (toolClassInstance != null) {
            this.toolClassInstance = toolClassInstance;
            toolDescription = Instances.TOOL_MANAGER.getToolManager().prepareToolPrompt(toolClassInstance
                    .getClass()
                    .getPackageName());
            this.toolMethods =
                    ReflectionUtility.getToolMethodsInToolClass(List.of(toolClassInstance.getClass()));
        }
        this.name = name;
        this.prompt = prompt;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onEnter(AgentContext context) {
    }

    @Override
    public void onExit(AgentContext context) {
    }

    @Override
    public String shouldTransition(Message message) {
        return null;
    }

    @Override
    public abstract Message process(Message message, AgentContext context) throws Exception;

    @Override
    public String toString() {
        return "Agent{name='" + name + "'}";
    }

    public String getPrompt() {
        return prompt;
    }

    @Override
    public String toolDescription() {
        return toolDescription;
    }

    public List<Method> getToolMethods() {
        return toolMethods;
    }

    public Object getToolClassInstance() {
        return toolClassInstance;
    }
}
