package com.grkn.orchestration.llms.fsm;

/**
 * Interface for validating transitions between agents.
 * Implementations can define custom rules for when transitions are allowed.
 */
public interface TransitionValidator {

    /**
     * Validates if a transition from one agent to another is allowed.
     *
     * @param fromAgent the source agent
     * @param toAgent the target agent
     * @param message the message triggering the transition
     * @param context the current execution context
     * @return validation result containing whether transition is allowed and reason if denied
     */
    TransitionValidationResult validate(Agent fromAgent, Agent toAgent, Message message, AgentContext context);

    /**
     * Result of a transition validation.
     */
    class TransitionValidationResult {
        private final boolean allowed;
        private final String reason;

        private TransitionValidationResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        /**
         * Creates a successful validation result.
         */
        public static TransitionValidationResult allow(String agentName) {
            return new TransitionValidationResult(true, agentName);
        }

        /**
         * Creates a failed validation result with a reason.
         */
        public static TransitionValidationResult deny(String reason) {
            return new TransitionValidationResult(false, reason);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }
    }
}
