package com.grkn.orchestration.llms.strategy;

import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.fsm.Message;

/**
 * Strategy for handling ask question actions.
 */
public class FinalizeStrategy implements ActionStrategy {
    @Override
    public Message execute(Message message, ApiResponse apiResponse, AbstractAgent abstractAgent) {
        message.setPayload(apiResponse);
        return message;
    }
}
