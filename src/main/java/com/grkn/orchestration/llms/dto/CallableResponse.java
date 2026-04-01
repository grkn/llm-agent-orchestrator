package com.grkn.orchestration.llms.dto;

public class CallableResponse {
    private Object response;
    private boolean isSuccess;
    private String toolName;
    private String error;

    public CallableResponse(Object response, boolean isSuccess, String toolName, String error) {
        this.response = response;
        this.isSuccess = isSuccess;
        this.toolName = toolName;
        this.error = error;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
