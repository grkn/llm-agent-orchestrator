package com.grkn.orchestration.llms.strategy;

import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.fsm.Message;

/**
 * Strategy interface for processing different action types.
 */
public interface ActionStrategy {
    /**
     * Processes the message based on the specific action strategy.
     *
     * @param message       the message to process
     * @param apiResponse   the API response containing action details
     * @param abstractAgent
     * @return the processed message
     * @throws Exception if processing fails
     */
    Message execute(Message message, ApiResponse apiResponse, AbstractAgent abstractAgent) throws Exception;
}
