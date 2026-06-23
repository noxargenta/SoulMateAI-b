package com.soulmate.config;

import java.io.*;
import java.util.Properties;

/**
 * 应用配置管理，使用 Properties 持久化用户设置到 config.properties。
 * 支持首次运行引导、配置读写和重置。
 */
public class AppConfig {
    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties;

    public AppConfig() {
        properties = new Properties();
        load();
    }

    private void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                System.err.println("加载配置文件失败: " + e.getMessage());
            }
        }
    }

    public void save() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "SoulMate AI Configuration");
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public boolean exists() {
        return new File(CONFIG_FILE).exists();
    }

    public void clear() {
        properties.clear();
        File f = new File(CONFIG_FILE);
        if (f.exists()) f.delete();
    }
}
