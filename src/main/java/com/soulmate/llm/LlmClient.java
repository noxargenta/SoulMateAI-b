package com.soulmate.llm;

import com.soulmate.model.Message;
import java.util.List;

/**
 * LLM 客户端接口抽象 — 策略模式。
 * 所有模型提供商（DeepSeek、Ollama 等）实现此接口。
 */
public interface LlmClient {
    /**
     * 发送聊天消息列表并获取模型回复文本。
     */
    String chat(List<Message> messages) throws Exception;
}
