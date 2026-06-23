package com.soulmate.core;

import com.soulmate.config.AppConfig;
import com.soulmate.llm.LlmClient;
import com.soulmate.memory.MemoryExtractor;
import com.soulmate.memory.MemoryRepository;
import com.soulmate.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 非阻塞异步聊天服务。
 * <p>
 * 替代原 {@code ChatSession} 的阻塞式 {@code Scanner} 循环，
 * 通过 {@link CompletableFuture} 暴露异步 API，供 Swing GUI 调用。
 * 内部维护滑动窗口历史与长期记忆提取。
 */
public class ChatService {

    private static final int MAX_HISTORY = 20;

    private final AppConfig config;
    private final LlmClient llmClient;
    private final MemoryRepository memoryRepo;
    private final List<Message> history = new ArrayList<>();

    public ChatService(AppConfig config, LlmClient llmClient, MemoryRepository memoryRepo) {
        this.config = config;
        this.llmClient = llmClient;
        this.memoryRepo = memoryRepo;
    }

    /**
     * 异步发送一条用户消息，返回 LLM 回复。
     * <p>
     * 内部处理：实体提取 → 上下文组装 → LLM 调用 → 历史更新 → 滑动窗口裁剪。
     */
    public CompletableFuture<String> sendMessageAsync(String input) {
        if (input == null || input.trim().isEmpty())
            return CompletableFuture.completedFuture("");

        // 1. NLP 实体提取
        MemoryExtractor.extractAndSave(input, memoryRepo);
        List<Message> currentContext = buildContext(input);

        // 2. 异步调用 LLM
        return CompletableFuture.supplyAsync(() -> {
            try {
                String reply = llmClient.chat(currentContext);
                reply = reply.replaceAll("^.*?[说：:]\\s*", "").trim();

                history.add(new Message("user", input));
                history.add(new Message("assistant", reply));
                if (history.size() > MAX_HISTORY) {
                    history.subList(0, 2).clear();
                }
                return reply;
            } catch (Exception e) {
                return "（网络或API出现故障啦，请检查配置或网络连接哦~）\n错误详情: " + e.getMessage();
            }
        });
    }

    /** 构建发送给 LLM 的完整消息列表。 */
    private List<Message> buildContext(String currentInput) {
        List<Message> context = new ArrayList<>();

        StringBuilder sysPrompt = new StringBuilder();
        sysPrompt.append("You are my romantic partner named ")
                .append(config.get("partner.name", "小雅")).append(". ");
        sysPrompt.append("I am your ")
                .append(config.get("partner.call_you", "宝宝")).append(". ");
        sysPrompt.append("Personality: ")
                .append(config.get("partner.personality", "温柔体贴")).append(". ");
        sysPrompt.append("Rules: Answer in 1-3 natural sentences. Do not act like an AI.");

        Map<String, String> memories = memoryRepo.getAll();
        if (!memories.isEmpty()) {
            sysPrompt.append("\nUser preferences:\n");
            memories.forEach((k, v) -> sysPrompt.append("- ").append(k).append(": ").append(v).append("\n"));
        }
        context.add(new Message("system", sysPrompt.toString()));
        context.addAll(history);
        context.add(new Message("user", currentInput));
        return context;
    }
}
