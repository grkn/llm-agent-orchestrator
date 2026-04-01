package com.grkn.orchestration.llms.core;

import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.interfaces.Client;
import com.grkn.orchestration.llms.properties.Properties;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class ChatGptClient implements Client {
    public final static Client INSTANCE = new ChatGptClient();
    private static final ObjectMapper objectMapper;
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    static {
        JsonMapper.Builder builder = JsonMapper.builder();
        objectMapper = builder.build();
    }

    private ChatGptClient() {}

    @Override
    public ApiResponse execute(Properties properties, String prompt, String responseId) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl()))
                    .timeout(java.time.Duration.ofSeconds(30));

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", properties.getOpenAIModel());
            payload.put("input", prompt);
            if(responseId != null) {
                payload.put("previous_response_id", responseId);
            }

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = builder
                    .header("Authorization", "Bearer " + properties.getOpenAIKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();


            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 400) {
                throw new IllegalStateException("LLM call failed: HTTP " + response.statusCode() + " -> " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode output = root.path("output");
            if (!output.isArray() || output.isEmpty()) {
                throw new IllegalStateException("LLM response missing output: " + response.body());
            }

            String text = output
                    .get(output.size() - 1)
                    .path("content")
                    .get(0)
                    .path("text")
                    .asString();

            ApiResponse apiResponse = objectMapper.readValue(text, ApiResponse.class);
            apiResponse.setResponseId(root.get("id").asString());
            return apiResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
