package com.soulmate;

import com.soulmate.config.AppConfig;
import com.soulmate.core.ChatSession;
import com.soulmate.llm.LlmClient;
import com.soulmate.llm.impl.DeepSeekClient;
import com.soulmate.llm.impl.OllamaClient;
import com.soulmate.memory.MemoryRepository;

import java.util.Scanner;

/**
 * SoulMate AI 主入口。
 * <p>
 * 启动流程：配置引导 → 依赖注入 → 启动对话会话。
 * 支持双模型切换（DeepSeek API / 本地 Ollama）。
 */
public class SoulMateApplication {

    public static void main(String[] args) {
        System.out.println("====== SoulMate AI v1.0 ======");
        AppConfig config = new AppConfig();
        Scanner scanner = new Scanner(System.in, "UTF-8");

        // =============================================
        // 首次运行配置引导
        // =============================================
        if (!config.exists()) {
            System.out.println("初次运行，请设置您的偏好：\n");

            // 模型选择
            System.out.print("请选择大模型驱动 (1. 本地 Ollama,  2. DeepSeek API): ");
            String engineChoice = scanner.nextLine().trim();
            config.set("llm.engine", engineChoice.equals("2") ? "deepseek" : "ollama");

            if (engineChoice.equals("2")) {
                System.out.print("请输入您的 DeepSeek API Key: ");
                config.set("llm.deepseek.apikey", scanner.nextLine().trim());
            } else {
                System.out.print("请输入 Ollama 地址 (默认 http://localhost:11434/api/chat): ");
                String url = scanner.nextLine().trim();
                config.set("llm.ollama.url", url.isEmpty() ? "http://localhost:11434/api/chat" : url);
                System.out.print("请输入本地模型名称 (默认 qwen2:1.5b): ");
                String model = scanner.nextLine().trim();
                config.set("llm.ollama.model", model.isEmpty() ? "qwen2:1.5b" : model);
            }

            // 角色设定
            System.out.print("伴侣名字 (默认 小雅): ");
            String name = scanner.nextLine().trim();
            config.set("partner.name", name.isEmpty() ? "小雅" : name);

            System.out.print("TA如何称呼你 (默认 宝宝): ");
            String call = scanner.nextLine().trim();
            config.set("partner.call_you", call.isEmpty() ? "宝宝" : call);

            System.out.print("TA的性格 (默认 温柔体贴，喜欢撒娇): ");
            String personality = scanner.nextLine().trim();
            config.set("partner.personality", personality.isEmpty() ? "温柔体贴，喜欢撒娇" : personality);

            config.save();
            System.out.println("✅ 配置已保存到 config.properties！");
        }

        // =============================================
        // 依赖注入（策略模式）
        // =============================================
        MemoryRepository memoryRepo = new MemoryRepository();
        LlmClient llmClient;

        String engine = config.get("llm.engine", "ollama");
        if ("deepseek".equalsIgnoreCase(engine)) {
            String apiKey = config.get("llm.deepseek.apikey", "");
            if (apiKey.isEmpty()) {
                System.err.println("错误: DeepSeek API Key 未配置，请输入 'restart' 重置配置。");
                System.exit(1);
            }
            llmClient = new DeepSeekClient(apiKey);
            System.out.println("🔌 已连接: DeepSeek API (deepseek-chat)");
        } else {
            String url = config.get("llm.ollama.url", "http://localhost:11434/api/chat");
            String model = config.get("llm.ollama.model", "qwen2:1.5b");
            llmClient = new OllamaClient(url, model);
            System.out.println("🔌 已连接: Ollama (" + model + ")");
        }

        // 启动核心对话
        ChatSession session = new ChatSession(config, llmClient, memoryRepo);
        session.start();
    }
}
