package com.soulmate.core;

import com.soulmate.config.AppConfig;
import com.soulmate.llm.LlmClient;
import com.soulmate.memory.MemoryExtractor;
import com.soulmate.memory.MemoryRepository;
import com.soulmate.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * 核心对话会话管理。
 * 管理对话生命周期、滑动窗口历史、记忆提取与注入。
 */
public class ChatSession {
    private static final Logger log = LoggerFactory.getLogger(ChatSession.class);
    private static final int MAX_HISTORY = 20;

    private final AppConfig config;
    private final LlmClient llmClient;
    private final MemoryRepository memoryRepo;
    private final List<Message> history = new ArrayList<>();

    public ChatSession(AppConfig config, LlmClient llmClient, MemoryRepository memoryRepo) {
        this.config = config;
        this.llmClient = llmClient;
        this.memoryRepo = memoryRepo;
    }

    /**
     * 进入主对话循环，支持 exit 退出 / restart 重置配置。
     */
    public void start() {
        String partnerName = config.get("partner.name", "小雅");
        String callYou = config.get("partner.call_you", "宝宝");
        System.out.println("\n💖 " + partnerName + " 已上线。 输入 'exit' 退出，输入 'restart' 重置所有配置。\n");

        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (true) {
            System.out.print("你：");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("💖 " + partnerName + ": " + callYou + " 拜拜～ ✨");
                break;
            }
            if ("restart".equalsIgnoreCase(input)) {
                config.clear();
                memoryRepo.clear();
                System.out.println("🔄 配置文件已清除，请重新启动程序。");
                System.exit(0);
            }
            if (input.isEmpty()) continue;

            // 1. NLP 解析并更新长期记忆
            MemoryExtractor.extractAndSave(input, memoryRepo);

            // 2. 组装上下文（系统 Prompt + 历史 + 当前输入）
            List<Message> currentContext = buildContext(input);

            // 3. 异步请求大模型
            System.out.print("💖 " + partnerName + " 正在输入");
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return llmClient.chat(currentContext);
                } catch (Exception e) {
                    log.error("API调用失败", e);
                    return "（网络信号不好呢，稍等一下下嘛~）错误: " + e.getMessage();
                }
            });

            // 等待期间显示加载动画
            while (!future.isDone()) {
                try { Thread.sleep(400); } catch (InterruptedException e) { break; }
                System.out.print(".");
            }
            System.out.println();

            String reply = future.join();

            // 4. 清理回复可能的前缀，存入历史
            reply = reply.replaceAll("^" + partnerName + "说[：:]?\\s*", "").trim();
            System.out.println("💖 " + partnerName + "：" + reply + "\n");

            history.add(new Message("user", input));
            history.add(new Message("assistant", reply));

            // 5. 滑动窗口 — 超出限制时丢弃最老的一轮
            if (history.size() > MAX_HISTORY) {
                history.subList(0, 2).clear();
            }
        }
    }

    /**
     * 构建发送给 LLM 的完整消息列表：系统 Prompt → 历史 → 当前输入。
     * 系统 Prompt 中动态注入长期记忆。
     */
    private List<Message> buildContext(String currentInput) {
        List<Message> context = new ArrayList<>();

        // 构建系统 Prompt
        StringBuilder sysPrompt = new StringBuilder();
        sysPrompt.append("You are my romantic partner named ")
                .append(config.get("partner.name", "小雅")).append(". ");
        sysPrompt.append("I am your ")
                .append(config.get("partner.call_you", "宝宝")).append(". ");
        sysPrompt.append("Personality: ")
                .append(config.get("partner.personality", "温柔体贴")).append(". ");
        sysPrompt.append("Rules: Answer in 1-3 sentences naturally in Chinese. Do NOT act like AI or customer service.");

        // 注入长期记忆
        Map<String, String> memories = memoryRepo.getAll();
        if (!memories.isEmpty()) {
            sysPrompt.append("\nUser preferences to remember:\n");
            memories.forEach((k, v) -> sysPrompt.append("- ").append(k).append(": ").append(v).append("\n"));
        }
        context.add(new Message("system", sysPrompt.toString()));

        // 注入对话历史（滑动窗口上下文）
        context.addAll(history);

        // 注入当前输入
        context.add(new Message("user", currentInput));
        return context;
    }
}
