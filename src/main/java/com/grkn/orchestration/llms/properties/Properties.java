package com.grkn.orchestration.llms.properties;

public class Properties {
    public static final Properties INSTANCE = new Properties();
    private Properties(){}

    public String getOpenAIKey(){
        return "API-KEY here";
    }

    public String getOpenAIModel(){
        return "gpt-4.1-mini";
    }

    public String getBaseUrl(){
        return "https://api.openai.com/v1/responses";
    }

}
