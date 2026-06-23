package com.soulmate.ui;

import com.soulmate.config.AppConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Swing 配置对话框。
 * <p>
 * 首次运行或点击「⚙️ 设置」时弹出，提供模型引擎选择（Ollama / DeepSeek）、
 * API Key 输入、伴侣角色名等核心配置项的编辑界面。
 */
public class ConfigDialog extends JDialog {

    private final AppConfig config;
    private boolean isSaved = false;

    public ConfigDialog(Frame parent, AppConfig config) {
        super(parent, "SoulMate AI - 核心配置", true);
        this.config = config;
        initUI();
    }

    private void initUI() {
        setSize(420, 380);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JRadioButton rbOllama = new JRadioButton("本地 Ollama",
                "ollama".equals(config.get("llm.engine", "ollama")));
        JRadioButton rbDeepSeek = new JRadioButton("DeepSeek API",
                "deepseek".equals(config.get("llm.engine", "")));
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbOllama);
        bg.add(rbDeepSeek);

        JTextField txtApiKey = new JTextField(config.get("llm.deepseek.apikey", ""));
        JTextField txtOllamaUrl = new JTextField(
                config.get("llm.ollama.url", "http://localhost:11434/api/chat"));
        JTextField txtModel = new JTextField(
                config.get("llm.ollama.model", "qwen2:1.5b"));
        JTextField txtName = new JTextField(config.get("partner.name", "小雅"));
        JTextField txtCallYou = new JTextField(config.get("partner.call_you", "宝宝"));
        JTextField txtPersonality = new JTextField(
                config.get("partner.personality", "温柔体贴，喜欢撒娇"));

        panel.add(new JLabel("模型引擎选择:"));
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.add(rbOllama);
        radioPanel.add(rbDeepSeek);
        panel.add(radioPanel);

        panel.add(new JLabel("DeepSeek API Key:"));
        panel.add(txtApiKey);
        panel.add(new JLabel("Ollama URL:"));
        panel.add(txtOllamaUrl);
        panel.add(new JLabel("Ollama 模型名:"));
        panel.add(txtModel);
        panel.add(new JLabel("TA的名字:"));
        panel.add(txtName);
        panel.add(new JLabel("TA称呼你:"));
        panel.add(txtCallYou);
        panel.add(new JLabel("TA的性格:"));
        panel.add(txtPersonality);

        add(panel, BorderLayout.CENTER);

        JButton btnSave = new JButton("保存配置并重启");
        btnSave.addActionListener(e -> {
            config.set("llm.engine", rbDeepSeek.isSelected() ? "deepseek" : "ollama");
            config.set("llm.deepseek.apikey", txtApiKey.getText().trim());
            config.set("llm.ollama.url", txtOllamaUrl.getText().trim());
            config.set("llm.ollama.model", txtModel.getText().trim());
            config.set("partner.name", txtName.getText().trim());
            config.set("partner.call_you", txtCallYou.getText().trim());
            config.set("partner.personality", txtPersonality.getText().trim());
            config.save();
            isSaved = true;
            dispose();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** 显示对话框并返回用户是否点击了保存。 */
    public boolean showDialog() {
        setVisible(true);
        return isSaved;
    }
}
