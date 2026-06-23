package com.soulmate;

import com.soulmate.ui.ChatWindow;

import javax.swing.*;

/**
 * SoulMate AI 桌面版主入口。
 * <p>
 * 设置系统原生外观后启动 Swing {@link ChatWindow}。
 * 所有配置引导和依赖注入交由 GUI 内部处理。
 */
public class SoulMateApplication {

    public static void main(String[] args) {
        // 使用系统原生外观（Windows / macOS 各自适配）
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // 不阻塞启动
        }

        SwingUtilities.invokeLater(() -> new ChatWindow().setVisible(true));
    }
}
