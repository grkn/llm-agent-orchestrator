package com.grkn.orchestration.llms.fsm;

import com.grkn.orchestration.llms.core.ChatGptClient;
import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.interfaces.Client;
import com.grkn.orchestration.llms.orchestrator.AgentOrchestrator;
import com.grkn.orchestration.llms.properties.Properties;
import com.grkn.orchestration.llms.strategy.ActionStrategy;
import com.grkn.orchestration.llms.strategy.ActionStrategyFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Builder for creating agents that extend AbstractAgent.
 * Provides a fluent API for defining agent behaviors without creating separate classes.
 */
public class AgentBuilder {

    private static final Logger logger = Logger.getLogger(AgentBuilder.class.getName());

    private String name;
    private String prompt;
    private Object toolClassInstance;
    private Consumer<AgentContext> onEnterHandler;
    private Consumer<AgentContext> onExitHandler;
    private Function<Message, String> transitionHandler;

    private AgentBuilder(String name) {
        this.name = name;
    }

    /**
     * Creates a new AgentBuilder with the specified name.
     *
     * @param name the agent name
     * @return a new AgentBuilder instance
     */
    public static AgentBuilder create(String name) {
        return new AgentBuilder(name);
    }

    public AgentBuilder toolInstance(Object toolClassInstance) {
        this.toolClassInstance = toolClassInstance;
        return this;
    }

    /**
     * Sets the name of the agent.
     *
     * @param name the agent name
     * @return this builder for chaining
     */
    public AgentBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AgentBuilder prompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Sets the onEnter handler called when the agent becomes active.
     *
     * @param handler the enter handler
     * @return this builder for chaining
     */
    public AgentBuilder onEnter(Consumer<AgentContext> handler) {
        this.onEnterHandler = handler;
        return this;
    }

    /**
     * Sets the onExit handler called when the agent is leaving.
     *
     * @param handler the exit handler
     * @return this builder for chaining
     */
    public AgentBuilder onExit(Consumer<AgentContext> handler) {
        this.onExitHandler = handler;
        return this;
    }

    /**
     * Builds the agent with the configured behaviors.
     *
     * @return a new Agent instance
     * @throws IllegalStateException if required handlers are not set
     */
    public Agent build() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("Agent name must be set");
        }
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalStateException("Agent name must be set");
        }

        return new AbstractAgent(name, toolClassInstance, prompt) {
            private final Client client = ChatGptClient.INSTANCE;

            @Override
            public void onEnter(AgentContext context) {
                if (onEnterHandler != null) {
                    onEnterHandler.accept(context);
                }
            }

            @Override
            public void onExit(AgentContext context) {
                if (onExitHandler != null) {
                    onExitHandler.accept(context);
                }
            }

            @Override
            public Message process(Message message, AgentContext context) throws Exception {
                String mainPrompt = buildPrompt(message, context);
                ApiResponse apiResponse = executeWithRetry(mainPrompt, context);
                return executeStrategy(message, apiResponse);
            }

            @Override
            public String shouldTransition(Message message, AgentContext context) {
                if (message.getType() == null
                    || Action.FINALIZE_TASK.name().equals(message.getPayload().getAction())
                    || message.getType().equals(context.getStateMachine().getCurrentAgent().getName())) {
                    // Decision logic for transition if the next agent is not specified or looped back to the same agent
                    return decideNextAgent(message, context);
                } else {

                    return message.getType();
                }
            }

            private String buildPrompt(Message message, AgentContext context) {
                String currentTask = message.getPayload() != null ? message.getPayload().getAnswer() : null;
                return AgentOrchestrator.mainPrompt.formatted(
                        prompt,
                        toolDescription(),
                        currentTask,
                        availableAgents(context),
                        findFinalizedAgents(message)
                );
            }

            private ApiResponse executeWithRetry(String prompt, AgentContext context) throws Exception {
                ApiResponse apiResponse = null;
                String responseIdKey = this.getName() + "-api-response-id";
                int retryCount = 0;
                while (retryCount < 5) {
                    String responseId = (String) context.getState(responseIdKey);
                    apiResponse = client.execute(Properties.INSTANCE, prompt, responseId);
                    context.setState(responseIdKey, apiResponse.getResponseId());

                    if (isValidAction(apiResponse.getAction())) {
                        return apiResponse;
                    }

                    retryCount++;
                    logger.info("Invalid action: " + apiResponse.getAction() + " - retrying...");
                }

                throw new IllegalStateException("Failed to get valid action after 3 retries");
            }

            private boolean isValidAction(String actionString) {
                try {
                    Action.valueOf(actionString);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }

            private Message executeStrategy(Message message, ApiResponse apiResponse) throws Exception {
                Action action = Action.valueOf(apiResponse.getAction());
                ActionStrategy strategy = ActionStrategyFactory.getStrategy(action);
                message.setPayload(apiResponse);
                return strategy.execute(message, apiResponse, this);
            }

            private String findFinalizedAgents(Message message) {
                Set<String> finalizedAgents = (Set<String>) message.getMetadata()
                        .getOrDefault("finalizedAgents", new HashSet<>());
                StringBuilder sb = new StringBuilder();
                for (String agent : finalizedAgents) {
                    sb.append("-").append(agent).append("\n");
                }
                return sb.toString();
            }

            private String availableAgents(AgentContext context) {
                StringBuilder sb = new StringBuilder();
                for (Agent allAgent : context.getStateMachine().getAllAgents()) {
                    sb.append("-").append(allAgent.getName()).append("\n");
                }
                return sb.toString();
            }

            private String decideNextAgent(Message message, AgentContext context) {
                int retryCount = 0;
                String nextAgent = "NO_AGENT";
                while (retryCount < 3) {
                    AgentStateMachine stateMachine = context.getStateMachine();
                    TransitionValidator.TransitionValidationResult transitionValidationResult = TransitionAgent.builder().build()
                            .decideTransition(stateMachine.getCurrentAgent(), stateMachine.getAgent(message.getType()),
                                    message, stateMachine.getContext());
                    if (transitionValidationResult.isAllowed()) {
                        // Type: agent name for next transition
                        nextAgent = transitionValidationResult.getReason();
                        break;
                    }

                    retryCount++;
                }
                // check weather next agent is valid
                for (Agent agent : context.getStateMachine().getAllAgents()) {
                    if(agent.getName().equals(nextAgent)) {
                        return nextAgent;
                    }
                }

                throw new IllegalStateException("Failed to get valid next agent after 3 retries");
            }
        };
    }
}
