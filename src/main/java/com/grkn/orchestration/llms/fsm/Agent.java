package com.grkn.orchestration.llms.fsm;

import java.util.List;

/**
 * Interface for agents in the FSM.
 * Each agent represents a mode/state of the system and can:
 * - Process incoming messages
 * - Send messages to other agents
 * - Define its behavior within the system
 */
public interface Agent {

    /**
     * Gets the unique name of this agent.
     *
     * @return the agent name
     */
    String getName();

    /**
     * Called when this agent becomes the active state.
     *
     * @param context the current execution context
     */
    void onEnter(AgentContext context);

    /**
     * Processes a message and determines the next action.
     *
     * @param message the incoming message
     * @param context the current execution context
     * @return the response message
     */
    Message process(Message message, AgentContext context) throws Exception;

    /**
     * Called when this agent is no longer the active state.
     *
     * @param context the current execution context
     */
    void onExit(AgentContext context);

    /**
     * Determines if this agent should transition based on the message.
     *
     * @param message the message to evaluate
     * @return the name of the next agent to transition to, or null to stay in current state
     */
    String shouldTransition(Message message);

    String toolDescription();

    String getPrompt();
}
