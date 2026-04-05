package com.grkn.orchestration.llms.fsm;

import com.grkn.orchestration.llms.core.ChatGptClient;
import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.properties.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Agent responsible for validating state transitions.
 * Checks if transitions are allowed based on configured rules and constraints.
 */
public class TransitionAgent {
    private static final Logger logger = Logger.getLogger(TransitionAgent.class.getName());

    private final List<TransitionValidator> validators;
    private final boolean strictMode;

    private TransitionAgent(Builder builder) {
        this.validators = builder.validators;
        this.strictMode = builder.strictMode;
    }

    /**
     * Validates a transition from one agent to another.
     *
     * @param fromAgent the source agent
     * @param toAgent the target agent
     * @param message the message triggering the transition
     * @param context the current execution context
     * @return validation result
     */
    public TransitionValidator.TransitionValidationResult decideTransition(
            Agent fromAgent, Agent toAgent, Message message, AgentContext context) {

        if (fromAgent == null || toAgent == null) {
            return TransitionValidator.TransitionValidationResult.deny("Source or target agent is null");
        }

        // Run all validators
        for (TransitionValidator validator : validators) {
            TransitionValidator.TransitionValidationResult result = validator.validate(fromAgent, toAgent, message, context);
            
            if (!result.isAllowed()) {
                if (strictMode) {
                    logger.warning(String.format(
                            "Transition denied by %s: %s -> %s. Reason: %s",
                            validator.getClass().getSimpleName(),
                            fromAgent.getName(),
                            toAgent.getName(),
                            result.getReason()
                    ));
                }
            } else {
                return result;
            }
        }

        logger.info(String.format("Transition allowed: %s -> %s", fromAgent.getName(), toAgent.getName()));
        return TransitionValidator.TransitionValidationResult.allow(toAgent.getName());
    }

    /**
     * Creates a new builder for TransitionAgent.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating TransitionAgent instances.
     */
    public static class Builder {
        private final List<TransitionValidator> validators = new ArrayList<>();
        private boolean strictMode = true;

        /**
         * Adds a validator to the transition agent.
         */
        public Builder addValidator(TransitionValidator validator) {
            this.validators.add(validator);
            return this;
        }

        /**
         * Sets strict mode. In strict mode, any validator failure prevents the transition.
         * In non-strict mode, warnings are logged but transitions may still proceed.
         */
        public Builder strictMode(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        /**
         * Builds the TransitionAgent.
         */
        public TransitionAgent build() {
            // Add default validator if none are configured
            if (validators.isEmpty()) {
                validators.add(new DefaultTransitionValidator());
            }
            return new TransitionAgent(this);
        }
    }

    /**
     * Default validator that checks basic transition rules.
     */
    private static class DefaultTransitionValidator implements TransitionValidator {
        @Override
        public TransitionValidationResult validate(Agent fromAgent, Agent toAgent, Message message, AgentContext context) {
            // Make a decision about the nextagent based on prompt
            AgentStateMachine stateMachine = context.getStateMachine();
            if (!stateMachine.getTransitions(fromAgent.getName()).contains(toAgent.getName())) {
                ApiResponse response = ChatGptClient.INSTANCE.execute(Properties.INSTANCE, """
                        Rules:
                        - Each agent has prompt and make a decision based on the prompt.
                        - Read prompt of current agent and available agents.
                        - Extract responsibility of available agents.
                        - Your question should be based on the prompt and the available agents.
                        - You can answer 'which agent is responsible for the decision message?'
                        - You will return agent name listed in available agents except current agent by filtering according to responsibility of agent.
                        - Decision message should be done based responsibility of agents
                        - You will choose the agent with the highest responsibility ratio than other agents.
                        - You can not choose current agent.
                        - You have to choose one agent except current agent or finalized task agents.
                        
                        
                        Available agents with prompt:
                        %s
                        
                        Current agent:
                        %s
                        
                        Finalized task agents:
                        %s
                        
                        Decision Message:
                        %s
                        

                        Result should be in the JSON format:
                        {
                            "agentName": "<agent name>",
                        }
                        """.formatted(
                        availableAgents(context),
                        fromAgent.getName(),
                        finalizedAgents((Set<String>) message.getMetadata("finalizedAgents")),
                        message.getPayload().getAnswer()), (String) context.getState("transitionAgent-response-id"));
                if (response == null || response.getAgentName() == null || response.getAgentName().isEmpty()) {
                    return TransitionValidationResult
                            .deny(("No agent is responsible for the decision. " +
                                   "You have to add agent for this question: %s")
                                    .formatted(message.getPayload().getAnswer()));
                } else {
                    context.setState("transitionAgent-response-id", response.getResponseId());
                    return TransitionValidationResult.allow(response.getAgentName());
                }
            }
            
            return TransitionValidationResult.allow(toAgent.getName());
        }

        private String finalizedAgents(Set<String> finalizedAgents) {
            if (finalizedAgents == null || finalizedAgents.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (String finalizedAgent : finalizedAgents) {
                sb.append("- ").append(finalizedAgent).append("\n");
            }
            return sb.toString();
        }

        private String availableAgents(AgentContext context) {
            StringBuilder sb = new StringBuilder();
            int count = 1;
            for (Agent allAgent : context.getStateMachine().getAllAgents()) {
                sb.append(count).append("-) ")
                        .append(allAgent.getName())
                        .append("\n\t")
                        .append("Prompt: ")
                        .append("\n\t\t")
                        .append(allAgent.getPrompt())
                        .append("\n");
                count++;
            }
            return sb.toString();
        }
    }
}
