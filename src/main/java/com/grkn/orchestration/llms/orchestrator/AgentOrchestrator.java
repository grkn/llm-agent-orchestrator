package com.grkn.orchestration.llms.orchestrator;

import com.grkn.orchestration.llms.core.ChatGptClient;
import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.fsm.*;
import com.grkn.orchestration.llms.properties.Properties;
import com.grkn.tool.library.core.DefaultToolManager;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Orchestrator for managing multi-agent communication and workflow.
 * Handles the coordination of multiple agents communicating via LLM requests.
 */
public class AgentOrchestrator {
    private static final Logger logger = Logger.getLogger(AgentOrchestrator.class.getName());
    public static final String mainPrompt= """
                        # Your Role and Goal
                        %s

                        # Available Tools
                        %s

                        # Current Task
                        %s

                        # Decision Framework

                        You must respond with ONLY a valid JSON object. Analyze the current task and choose ONE action:

                        ## Action Types

                        1. **RUN_TOOL_SEQUENTIAL** - Execute tools one after another when:
                           - Output of one tool is needed as input for the next
                           - Tasks have dependencies
                           - Example: Read file → Process content → Write result

                        2. **RUN_TOOL_PARALLEL** - Execute multiple tools concurrently when:
                           - Tools are independent and don't depend on each other
                           - Multiple similar operations needed simultaneously
                           - Example: Read multiple files at once, write to multiple locations

                        3. **ASK_AGENT** - Delegate to another agent when:
                           - Task requires specialized knowledge/skills of another agent
                           - You need information or help from another agent
                           - Collaboration is needed to complete the task

                        4. **FINALIZE_TASK** - Complete the workflow when:
                           - The main goal has been fully accomplished
                           - All work is done and you have the final answer
                           - No further actions are needed

                        ## Important Rules

                        - Always keep the main goal in focus, even when handling sub-tasks
                        - If you receive a new question or work item, treat it as a priority sub-task
                        - Choose the most efficient action based on the current situation
                        - Only use tools that are listed in the "Available Tools" section
                        - For ASK_AGENT, specify the target agent name and your question in the "answer" field

                        ## Required JSON Response Format

                        Return ONLY valid JSON with this exact structure:

                        {
                          "action": "<ONE OF: RUN_TOOL_SEQUENTIAL, RUN_TOOL_PARALLEL, ASK_AGENT, FINALIZE_TASK>",
                          "toolNames": ["tool1", "tool2"],
                          "inputs": [{"param1": "value1"}, {"param2": "value2"}],
                          "answer": "<your answer, question, or final result>",
                          "agentName": "<target agent name for ASK_AGENT>",
                          "toolOutput": "<tool execution results as JSON string>"
                        }

                        ## Field Usage Guide

                        - **action** (required): Must be one of the four action types
                        - **toolNames** (required for RUN_TOOL_*): Array of tool names to execute
                        - **inputs** (required for RUN_TOOL_*): Array of parameter objects, one per tool
                        - **answer** (required for ASK_AGENT, FINALIZE_TASK): Your message, question, or final result
                        - **agentName** (required for ASK_AGENT): Name of the agent you want to delegate to
                        - **toolOutput** (system-managed): Tool execution results (you will see this in subsequent iterations)

                        ## Examples

                        Sequential tool execution:
                        {
                          "action": "RUN_TOOL_SEQUENTIAL",
                          "toolNames": ["READ_FILE", "SEARCH_PATTERN_IN_FILE"],
                          "inputs": [{"path": "C:\\\\repo\\\\App.java"}, {"filePath": "C:\\\\repo\\\\App.java", "pattern": "main"}]
                        }

                        Parallel tool execution:
                        {
                          "action": "RUN_TOOL_PARALLEL",
                          "toolNames": ["READ_FILE", "READ_FILE", "READ_FILE"],
                          "inputs": [{"path": "file1.txt"}, {"path": "file2.txt"}, {"path": "file3.txt"}]
                        }

                        Delegating to another agent:
                        {
                          "action": "ASK_AGENT",
                          "agentName": "architect",
                          "answer": "What design pattern should I use for the authentication module?"
                        }

                        Finalizing the task:
                        {
                          "action": "FINALIZE_TASK",
                          "answer": "Calculator application successfully implemented with JavaFX GUI. All features tested and working."
                        }

                        Now, analyze the current task and respond with your JSON decision.
                        """;

    private final AgentStateMachine stateMachine;
    private final int maxIterations;
    private final boolean enableLogging;

    private AgentOrchestrator(Builder builder) {
        this.stateMachine = builder.stateMachine;
        this.maxIterations = builder.maxIterations;
        this.enableLogging = builder.enableLogging;
    }

    /**
     * Processes a task through the agent orchestration workflow.
     * Agents communicate with LLMs and pass messages until task completion.
     *
     * @return the final response after orchestration completes
     * @throws Exception if orchestration fails
     */
    public ApiResponse process() throws Exception {
        if (enableLogging) {
            logger.info("Starting orchestration workflow");
        }

        // Start the state machine
        stateMachine.start();

        // Create initial message
        Message currentMessage = Message.builder(stateMachine.getCurrentAgent().getName())
                .sender("orchestrator")
                .metadata("iteration", 0)
                .build();
        int iteration = 0;

        // Main orchestration loop
        while (iteration < maxIterations) {
            iteration++;

            if (enableLogging) {
                logger.info(String.format("Iteration %d - Current Agent: %s",
                    iteration, stateMachine.getCurrentAgent().getName()));
            }

            // Process message through current agent

            currentMessage = stateMachine.processMessage(currentMessage);

            // Check if task is finalized
            if (isTaskComplete(currentMessage)) {
                if (enableLogging) {
                    logger.info("Task completed successfully");
                }
                break;
            }

            // Check if we need to route to another agent
            String nextAgent = currentMessage.getType();
            if (nextAgent != null && !nextAgent.equals(stateMachine.getCurrentAgent().getName())) {
                if (enableLogging) {
                    logger.info(String.format("Routing to agent: %s", nextAgent));
                }

                // Update message for next agent
                currentMessage = Message.builder(nextAgent)
                        .payload(currentMessage.getPayload())
                        .sender(stateMachine.getCurrentAgent().getName())
                        .metadata("iteration", iteration)
                        .build();
            }

            // Prevent infinite loops
            if (iteration >= maxIterations) {
                logger.warning("Max iterations reached. Stopping orchestration.");
                break;
            }
        }

        return currentMessage.getPayload();
    }

    /**
     * Checks if the task is complete based on the message action.
     *
     * @param message the message to check
     * @return true if task is complete
     */
    private boolean isTaskComplete(Message message) {
        ApiResponse response = message.getPayload();
        if (response == null || response.getAction() == null) {
            return false;
        }

        try {
            Action action = Action.valueOf(response.getAction());
            return action == Action.FINALIZE_TASK;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Creates a new builder for AgentOrchestrator.
     *
     * @param agents list of agents to orchestrate
     * @return builder instance
     */
    public static Builder builder(List<Agent> agents) {
        return new Builder(agents);
    }

    /**
     * Builder for creating AgentOrchestrator instances.
     */
    public static class Builder {
        private final AgentStateMachine stateMachine;
        private int maxIterations = 100;
        private boolean enableLogging = true;

        private Builder(List<Agent> agents) {
            if (agents == null || agents.isEmpty()) {
                throw new IllegalArgumentException("Agent list cannot be empty");
            }

            this.stateMachine = new AgentStateMachine();

            // Register all agents
            for (Agent agent : agents) {
                stateMachine.registerAgent(agent);
            }

            // Set first agent as initial
            stateMachine.setInitialAgent(agents.get(0).getName());
        }

        /**
         * Sets the maximum number of iterations before stopping.
         *
         * @param maxIterations max iterations
         * @return this builder
         */
        public Builder maxIterations(int maxIterations) {
            if (maxIterations <= 0) {
                throw new IllegalArgumentException("Max iterations must be positive");
            }
            this.maxIterations = maxIterations;
            return this;
        }

        /**
         * Enables or disables logging.
         *
         * @param enableLogging true to enable logging
         * @return this builder
         */
        public Builder enableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        /**
         * Sets the initial agent by name.
         *
         * @param agentName initial agent name
         * @return this builder
         */
        public Builder initialAgent(String agentName) {
            stateMachine.setInitialAgent(agentName);
            return this;
        }

        /**
         * Adds a transition between two agents.
         *
         * @param fromAgent source agent name
         * @param toAgent target agent name
         * @return this builder
         */
        public Builder addTransition(String fromAgent, String toAgent) {
            stateMachine.addTransition(fromAgent, toAgent);
            return this;
        }

        /**
         * Allows an agent to call itself recursively.
         *
         * @param agentName the agent name
         * @return this builder
         */
        public Builder allowRecursive(String agentName) {
            stateMachine.addRecursiveTransition(agentName);
            return this;
        }

        /**
         * Adds a transition listener.
         *
         * @param listener the transition listener
         * @return this builder
         */
        public Builder addTransitionListener(AgentStateMachine.TransitionListener listener) {
            stateMachine.addTransitionListener(listener);
            return this;
        }

        /**
         * Builds the orchestrator.
         *
         * @return configured AgentOrchestrator instance
         */
        public AgentOrchestrator build() {
            return new AgentOrchestrator(this);
        }
    }

    /**
     * Gets the underlying state machine.
     *
     * @return the state machine
     */
    public AgentStateMachine getStateMachine() {
        return stateMachine;
    }
}
