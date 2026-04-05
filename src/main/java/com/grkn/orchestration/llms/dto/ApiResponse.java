package com.grkn.orchestration.llms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse {
    private String action;
    private String agentName;
    private List<String> toolNames;
    private List<Map<String, String>> inputs;
    private String answer;
    private String toolOutput;
    private String responseId;

    public ApiResponse(String action, String agentName,List<String> toolNames, List<Map<String, String>> inputs, String answer, String toolOutput) {
        this.action = action;
        this.agentName = agentName;
        this.toolNames = toolNames;
        this.inputs = inputs;
        this.answer = answer;
        this.toolOutput = toolOutput;
    }

    public ApiResponse() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getToolOutput() {
        return toolOutput;
    }

    public void setToolOutput(String toolOutput) {
        this.toolOutput = toolOutput;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public List<String> getToolNames() {
        return toolNames;
    }

    public List<Map<String, String>> getInputs() {
        return inputs;
    }

    public void setInputs(List<Map<String, String>> inputs) {
        this.inputs = inputs;
    }

    public void setToolNames(List<String> toolNames) {
        this.toolNames = toolNames;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "action='" + action + '\'' +
                ", agentName='" + agentName + '\'' +
                ", toolNames='" + toolNames + '\'' +
                ", answer='" + answer + '\'' +
                ", toolOutput='" + toolOutput + '\'' +
                ", responseId='" + responseId + '\'' +
                '}';
    }
}
