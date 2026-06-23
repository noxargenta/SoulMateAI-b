package com.soulmate.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soulmate.llm.LlmClient;
import com.soulmate.model.Message;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Ollama 本地模型客户端实现。
 * 对接 Ollama /api/chat 端点。
 */
public class OllamaClient implements LlmClient {

    private final String url;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public OllamaClient(String url, String model) {
        this.url = url;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String chat(List<Message> messages) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("stream", false);

        ArrayNode msgsNode = body.putArray("messages");
        for (Message m : messages) {
            ObjectNode mNode = mapper.createObjectNode();
            mNode.put("role", m.getRole());
            mNode.put("content", m.getContent());
            msgsNode.add(mNode);
        }

        String requestBody = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama API Error: " + response.statusCode()
                    + " - " + response.body());
        }

        JsonNode rootNode = mapper.readTree(response.body());
        return rootNode.path("message").path("content").asText();
    }
}
