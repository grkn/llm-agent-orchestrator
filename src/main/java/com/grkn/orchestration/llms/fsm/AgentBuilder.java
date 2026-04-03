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
import java.util.List;
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

    /**
     * Sets the instance of the tool class.
     *
     * @param instance of the tool class
     * @return this builder for chaining
     */
    public AgentBuilder name(Object instance) {
        this.toolClassInstance = instance;
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
                boolean actionRetry = true;
                ApiResponse apiResponse = null;
                Action action = Action.ASK_AGENT;
                // TO make it robust retrying will handle invalid action
                String mainPrompt = AgentOrchestrator.mainPrompt.formatted(prompt, toolDescription(),
                        message.getPayload() != null ? message.getPayload().getAnswer() : null
                        , availableAgents(context)
                        , findFinalizedAgents(message));
                while (actionRetry) {
                    apiResponse = client.execute(Properties.INSTANCE,
                            mainPrompt,
                            message.getPayload() != null ? message.getPayload().getResponseId() : null);
                    try {
                        action = Action.valueOf(apiResponse.getAction());
                        actionRetry = false;
                    } catch (IllegalArgumentException e) {
                        logger.info("Invalid action: " + apiResponse.getAction() + " retrying...");
                    }
                }

                ActionStrategy strategy = ActionStrategyFactory.getStrategy(action);
                message.setPayload(apiResponse);
                return strategy.execute(message, apiResponse , this);
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

            @Override
            public String shouldTransition(Message message) {
                // Type: agent name for next transition
                // if there is noonext transition then it will stay in the same agent to think about the next action
                return message.getType() == null ? this.getName() : message.getType();
            }
        };
    }
}
