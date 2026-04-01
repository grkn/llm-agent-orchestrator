package com.grkn.orchestration.llms.fsm;

import com.grkn.orchestration.llms.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a message passed between agents in the FSM.
 * Messages carry data and metadata for inter-agent communication.
 */
public class Message {
    private final String type;
    private ApiResponse payload;
    private final String sender;
    private final Map<String, Object> metadata;

    private Message(Builder builder) {
        this.type = builder.type;
        this.payload = builder.payload;
        this.sender = builder.sender;
        this.metadata = new HashMap<>(builder.metadata);
    }

    public String getType() {
        return type;
    }

    public ApiResponse getPayload() {
        return payload;
    }

    public void setPayload(ApiResponse payload) {
        this.payload = payload;
    }

    public <T> T getPayload(Class<T> clazz) {
        return clazz.cast(payload);
    }

    public String getSender() {
        return sender;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    @Override
    public String toString() {
        return "Message{type='" + type + "', sender='" + sender + "', payload=" + payload + "}";
    }

    public static Builder builder(String type) {
        return new Builder(type);
    }

    public static class Builder {
        private final String type;
        private ApiResponse payload;
        private String sender;
        private final Map<String, Object> metadata = new HashMap<>();

        private Builder(String type) {
            this.type = type;
        }

        public Builder payload(ApiResponse payload) {
            this.payload = payload;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}
