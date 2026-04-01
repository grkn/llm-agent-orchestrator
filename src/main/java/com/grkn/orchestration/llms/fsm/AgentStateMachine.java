package com.grkn.orchestration.llms.fsm;

import java.util.*;

/**
 * Finite State Machine for managing communicating agents.
 * Agents represent states and can communicate with each other according to defined behaviors.
 */
public class AgentStateMachine {
    private final Map<String, Agent> agents;
    private final Map<String, Set<String>> transitions;
    private Agent currentAgent;
    private Agent initialAgent;
    private final AgentContext context;
    private final List<TransitionListener> listeners;

    public AgentStateMachine() {
        this.agents = new HashMap<>();
        this.transitions = new HashMap<>();
        this.context = new AgentContext(this);
        this.listeners = new ArrayList<>();
    }

    /**
     * Registers an agent in the FSM.
     *
     * @param agent the agent to register
     * @return this state machine for chaining
     * @throws IllegalArgumentException if an agent with this name already exists
     */
    public AgentStateMachine registerAgent(Agent agent) {
        String name = agent.getName();
        if (agents.containsKey(name)) {
            throw new IllegalArgumentException("Agent '" + name + "' is already registered");
        }

        agents.put(name, agent);
        transitions.putIfAbsent(name, new HashSet<>());

        // Set as initial agent if it's the first one
        if (initialAgent == null) {
            initialAgent = agent;
            currentAgent = agent;
        }

        return this;
    }

    /**
     * Adds a transition between two agents.
     *
     * @param fromAgent the source agent name
     * @param toAgent   the target agent name
     * @return this state machine for chaining
     * @throws IllegalArgumentException if either agent doesn't exist
     */
    public AgentStateMachine addTransition(String fromAgent, String toAgent) {
        if (!agents.containsKey(fromAgent)) {
            throw new IllegalArgumentException("Agent '" + fromAgent + "' is not registered");
        }
        if (!agents.containsKey(toAgent)) {
            throw new IllegalArgumentException("Agent '" + toAgent + "' is not registered");
        }

        transitions.computeIfAbsent(fromAgent, k -> new HashSet<>()).add(toAgent);
        return this;
    }

    /**
     * Adds a recursive transition (agent can transition to itself).
     *
     * @param agentName the agent name
     * @return this state machine for chaining
     */
    public AgentStateMachine addRecursiveTransition(String agentName) {
        return addTransition(agentName, agentName);
    }

    /**
     * Sets the initial agent.
     *
     * @param agentName the initial agent name
     * @return this state machine for chaining
     * @throws IllegalArgumentException if the agent doesn't exist
     */
    public AgentStateMachine setInitialAgent(String agentName) {
        Agent agent = agents.get(agentName);
        if (agent == null) {
            throw new IllegalArgumentException("Agent '" + agentName + "' is not registered");
        }
        this.initialAgent = agent;
        return this;
    }

    /**
     * Starts the state machine by entering the initial agent.
     */
    public void start() {
        if (initialAgent == null) {
            throw new IllegalStateException("No initial agent set");
        }
        currentAgent = initialAgent;
        currentAgent.onEnter(context);
    }

    /**
     * Processes a message through the current agent.
     *
     * @param message the message to process
     * @return the response message
     */
    public Message processMessage(Message message) throws Exception {
        if (currentAgent == null) {
            throw new IllegalStateException("State machine not started. Call start() first.");
        }

        // Process the message
        Message response = currentAgent.process(message, context);

        // Check if the agent wants to transition
        String nextAgentName = currentAgent.shouldTransition(response);

        if (nextAgentName != null && !nextAgentName.equals(currentAgent.getName())) {
            transitionTo(nextAgentName);
        }

        return response;
    }

    /**
     * Routes a message to a specific agent (internal communication).
     *
     * @param targetAgentName the target agent name
     * @param message         the message to send
     * @return the response message
     */
    Message routeMessage(String targetAgentName, Message message) throws Exception {
        Agent targetAgent = agents.get(targetAgentName);
        if (targetAgent == null) {
            throw new IllegalArgumentException("Agent '" + targetAgentName + "' is not registered");
        }

        // Check if transition is allowed
        Set<String> allowedTransitions = transitions.get(currentAgent.getName());
        if (allowedTransitions == null || !allowedTransitions.contains(targetAgentName)) {
            throw new IllegalStateException(
                    "Transition from '" + currentAgent.getName() + "' to '" + targetAgentName + "' is not allowed"
            );
        }

        return targetAgent.process(message, context);
    }

    /**
     * Transitions to a different agent.
     *
     * @param agentName the target agent name
     * @throws IllegalArgumentException if the agent doesn't exist
     * @throws IllegalStateException    if the transition is not allowed
     */
    public void transitionTo(String agentName) {
        Agent nextAgent = agents.get(agentName);
        if (nextAgent == null) {
            throw new IllegalArgumentException("Agent '" + agentName + "' is not registered");
        }

        Set<String> allowedTransitions = transitions.get(currentAgent.getName());
        if (allowedTransitions == null || !allowedTransitions.contains(agentName)) {
            throw new IllegalStateException(
                    "Transition from '" + currentAgent.getName() + "' to '" + agentName + "' is not allowed"
            );
        }

        Agent previousAgent = currentAgent;

        notifyListeners(previousAgent, nextAgent);

        currentAgent.onExit(context);
        currentAgent = nextAgent;
        currentAgent.onEnter(context);
    }

    /**
     * Gets the current active agent.
     *
     * @return the current agent
     */
    public Agent getCurrentAgent() {
        return currentAgent;
    }

    /**
     * Gets an agent by name.
     *
     * @param name the agent name
     * @return the agent, or null if not found
     */
    public Agent getAgent(String name) {
        return agents.get(name);
    }

    /**
     * Gets all registered agents.
     *
     * @return collection of all agents
     */
    public Collection<Agent> getAllAgents() {
        return new ArrayList<>(agents.values());
    }

    /**
     * Gets allowed transitions from an agent.
     *
     * @param agentName the agent name
     * @return set of target agent names
     */
    public Set<String> getTransitions(String agentName) {
        Set<String> agentTransitions = transitions.get(agentName);
        return agentTransitions != null ? new HashSet<>(agentTransitions) : Collections.emptySet();
    }

    /**
     * Resets the state machine to the initial agent.
     */
    public void reset() {
        if (currentAgent != null && currentAgent != initialAgent) {
            currentAgent.onExit(context);
        }
        currentAgent = initialAgent;
        context.clearState();
        if (currentAgent != null) {
            currentAgent.onEnter(context);
        }
    }

    /**
     * Adds a transition listener.
     *
     * @param listener the listener to add
     */
    public void addTransitionListener(TransitionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a transition listener.
     *
     * @param listener the listener to remove
     */
    public void removeTransitionListener(TransitionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Agent from, Agent to) {
        for (TransitionListener listener : listeners) {
            listener.onTransition(from, to);
        }
    }

    /**
     * Listener interface for state transitions.
     */
    public interface TransitionListener {
        void onTransition(Agent from, Agent to);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AgentStateMachine{\n");
        sb.append("  Agents: ").append(agents.size()).append("\n");
        sb.append("  Initial Agent: ").append(initialAgent != null ? initialAgent.getName() : "none").append("\n");
        sb.append("  Current Agent: ").append(currentAgent != null ? currentAgent.getName() : "none").append("\n");
        sb.append("  Transitions:\n");

        for (Map.Entry<String, Set<String>> entry : transitions.entrySet()) {
            for (String target : entry.getValue()) {
                sb.append("    ").append(entry.getKey()).append(" --> ").append(target).append("\n");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
