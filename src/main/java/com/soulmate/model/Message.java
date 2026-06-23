package com.soulmate.model;

/**
 * 统一消息模型，适配标准 Chat API 的对话结构。
 * 一条消息包含角色 (system/user/assistant) 和文本内容。
 */
public class Message {
    private String role;
    private String content;

    public Message() {}

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
