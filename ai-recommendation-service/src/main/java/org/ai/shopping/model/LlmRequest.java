package org.ai.shopping.model;

import lombok.Data;

import java.util.List;

@Data
public class LlmRequest {

    private String model;
    private List<Message> messages;
    private double temperature;

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public LlmRequest(String model, List<Message> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public String getModel() {
        return model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public double getTemperature() {
        return temperature;
    }
}
