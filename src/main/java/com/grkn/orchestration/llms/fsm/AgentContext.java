package com.grkn.orchestration.llms.fsm;

import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed to agents during execution.
 * Provides access to the state machine and shared state.
 */
public class AgentContext {
    private final AgentStateMachine stateMachine;
    private final Map<String, Object> sharedState;

    public AgentContext(AgentStateMachine stateMachine) {
        this.stateMachine = stateMachine;
        this.sharedState = new HashMap<>();
    }

    /**
     * Gets the state machine managing this agent.
     *
     * @return the state machine
     */
    public AgentStateMachine getStateMachine() {
        return stateMachine;
    }

    /**
     * Sends a message to a specific agent.
     *
     * @param targetAgent the target agent name
     * @param message     the message to send
     * @return the response message
     */
    public Message sendMessage(String targetAgent, Message message) throws Exception {
        return stateMachine.routeMessage(targetAgent, message);
    }

    /**
     * Sets a value in the shared state.
     *
     * @param key   the state key
     * @param value the state value
     */
    public void setState(String key, Object value) {
        sharedState.put(key, value);
    }

    /**
     * Gets a value from the shared state.
     *
     * @param key the state key
     * @return the state value, or null if not found
     */
    public Object getState(String key) {
        return sharedState.get(key);
    }

    /**
     * Gets a typed value from the shared state.
     *
     * @param key   the state key
     * @param clazz the expected type
     * @param <T>   the type parameter
     * @return the typed state value
     */
    public <T> T getState(String key, Class<T> clazz) {
        Object value = sharedState.get(key);
        return value != null ? clazz.cast(value) : null;
    }

    /**
     * Checks if a key exists in the shared state.
     *
     * @param key the state key
     * @return true if the key exists
     */
    public boolean hasState(String key) {
        return sharedState.containsKey(key);
    }

    /**
     * Removes a value from the shared state.
     *
     * @param key the state key
     */
    public void removeState(String key) {
        sharedState.remove(key);
    }

    /**
     * Clears all shared state.
     */
    public void clearState() {
        sharedState.clear();
    }

    /**
     * Gets all shared state.
     *
     * @return a copy of the shared state map
     */
    public Map<String, Object> getAllState() {
        return new HashMap<>(sharedState);
    }
}
