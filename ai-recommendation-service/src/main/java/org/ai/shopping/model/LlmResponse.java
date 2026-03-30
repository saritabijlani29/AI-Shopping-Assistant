package org.ai.shopping.model;

import lombok.Data;

import java.util.List;

@Data
public class LlmResponse {

    public List<Choice> choices;

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String content;
    }
}
