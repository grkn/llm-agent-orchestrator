package com.grkn.orchestration.llms.properties;

public class Properties {
    public static final Properties INSTANCE = new Properties();

    private String openAIKey;
    private String openAIModel;
    private String baseUrl;

    private Properties(){}

    public String getOpenAIKey() {
        return openAIKey;
    }

    public void setOpenAIKey(String openAIKey) {
        this.openAIKey = openAIKey;
    }

    public String getOpenAIModel() {
        return openAIModel;
    }

    public void setOpenAIModel(String openAIModel) {
        this.openAIModel = openAIModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
