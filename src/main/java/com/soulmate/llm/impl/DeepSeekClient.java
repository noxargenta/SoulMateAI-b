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
 * DeepSeek API 客户端实现。
 * 对接 DeepSeek Chat API 的标准 OpenAI 兼容接口。
 */
public class DeepSeekClient implements LlmClient {

    /** DeepSeek API 端点（via https://api-docs.deepseek.com/zh-cn/） */
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public DeepSeekClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String chat(List<Message> messages) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "deepseek-chat");
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
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("DeepSeek API Error: " + response.statusCode()
                    + " - " + response.body());
        }

        JsonNode rootNode = mapper.readTree(response.body());
        return rootNode.path("choices").get(0)
                .path("message").path("content").asText();
    }
}
