package com.grkn.orchestration.llms.strategy;

import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.fsm.AbstractAgent;
import com.grkn.orchestration.llms.fsm.Message;

/**
 * Strategy for delegating to another agent.
 */
public class AskAgentStrategy implements ActionStrategy {
    @Override
    public Message execute(Message message, ApiResponse apiResponse, AbstractAgent abstractAgent) {
        return Message.builder(apiResponse.getAgentName())
                .payload(message.getPayload())
                .build();
    }
}
