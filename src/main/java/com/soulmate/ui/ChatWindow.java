package com.soulmate.ui;

import com.soulmate.config.AppConfig;
import com.soulmate.core.ChatService;
import com.soulmate.llm.impl.DeepSeekClient;
import com.soulmate.llm.impl.OllamaClient;
import com.soulmate.memory.MemoryRepository;

import javax.swing.*;
import java.awt.*;

/**
 * SoulMate AI 主聊天窗口。
 * <p>
 * 上半部分为可滚动的聊天记录区，下半部分为输入栏 + 发送 / 设置按钮。
 * 所有 LLM 调用通过 {@link ChatService#sendMessageAsync} 异步执行，
 * 不让 UI 线程阻塞。
 */
public class ChatWindow extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private ChatService chatService;
    private AppConfig config;
    private String partnerName;

    public ChatWindow() {
        config = new AppConfig();

        // 首次运行 → 弹出配置对话框
        if (!config.exists()) {
            boolean saved = new ConfigDialog(this, config).showDialog();
            if (!saved) System.exit(0);
        }

        initService();
        initUI();
    }

    /** 根据配置实例化 LLM 客户端与 ChatService。 */
    private void initService() {
        partnerName = config.get("partner.name", "小雅");
        MemoryRepository memoryRepo = new MemoryRepository();
        String engine = config.get("llm.engine", "ollama");

        try {
            if ("deepseek".equals(engine)) {
                chatService = new ChatService(config,
                        new DeepSeekClient(config.get("llm.deepseek.apikey", "")),
                        memoryRepo);
            } else {
                chatService = new ChatService(config,
                        new OllamaClient(config.get("llm.ollama.url", ""),
                                config.get("llm.ollama.model", "")),
                        memoryRepo);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "客户端初始化失败，请检查配置:\n" + e.getMessage(),
                    "启动错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initUI() {
        setTitle("SoulMate AI - 和 " + partnerName + " 聊天中");
        setSize(520, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 聊天记录区
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // 底部输入面板
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("发送 (Enter)");
        sendButton.addActionListener(e -> sendMessage());

        JButton settingButton = new JButton("⚙️ 设置");
        settingButton.addActionListener(e -> {
            if (new ConfigDialog(this, config).showDialog()) {
                JOptionPane.showMessageDialog(this,
                        "配置已更新，重启程序后生效！");
                System.exit(0);
            }
        });

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        btnPanel.add(sendButton);
        btnPanel.add(settingButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        appendMessage("系统", "💖 " + partnerName + " 已上线，来聊天吧～");
    }

    /** 发送消息：清空输入框 → 异步调用 LLM → 回调更新 UI。 */
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        inputField.setText("");
        appendMessage("你", text);
        setControlsEnabled(false);

        chatService.sendMessageAsync(text).thenAccept(reply -> {
            SwingUtilities.invokeLater(() -> {
                appendMessage(partnerName, reply);
                setControlsEnabled(true);
            });
        });
    }

    /** 向聊天记录区追加一行消息。 */
    private void appendMessage(String sender, String message) {
        chatArea.append("【" + sender + "】 " + message + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /** 发送期间禁用输入/发送按钮，防止重复提交。 */
    private void setControlsEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        sendButton.setText(enabled ? "发送 (Enter)" : "对方正在输入...");
        if (enabled) inputField.requestFocus();
    }
}
