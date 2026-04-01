package com.grkn.orchestration.llms.strategy;

import com.grkn.orchestration.llms.enums.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing action strategies.
 */
public class ActionStrategyFactory {
    private static final Map<Action, ActionStrategy> strategies = new HashMap<>();

    static {
        strategies.put(Action.RUN_TOOL_PARALLEL, new RunToolParallelStrategy());
        strategies.put(Action.RUN_TOOL_SEQUENTIAL, new RunToolSequentialStrategy());
        strategies.put(Action.ASK_AGENT, new AskAgentStrategy());
        strategies.put(Action.FINALIZE_TASK, new FinalizeStrategy()); // Same behavior as ASK_QUESTION
    }

    /**
     * Gets the appropriate strategy for the given action.
     *
     * @param action the action type
     * @return the corresponding strategy
     * @throws IllegalArgumentException if no strategy exists for the action
     */
    public static ActionStrategy getStrategy(Action action) {
        ActionStrategy strategy = strategies.get(action);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for action: " + action);
        }
        return strategy;
    }
}
