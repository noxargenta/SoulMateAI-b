package com.soulmate.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 结构化记忆仓库，使用 JSON 文件持久化用户偏好与个人信息。
 * 替代原有的 Base64 伪加密方案。
 */
public class MemoryRepository {
    private static final String MEMORY_FILE = "memory.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, String> memoryMap;

    public MemoryRepository() {
        load();
    }

    private void load() {
        File file = new File(MEMORY_FILE);
        if (file.exists()) {
            try {
                memoryMap = mapper.readValue(file, new TypeReference<Map<String, String>>(){});
            } catch (Exception e) {
                System.err.println("加载记忆文件失败: " + e.getMessage());
                memoryMap = new HashMap<>();
            }
        } else {
            memoryMap = new HashMap<>();
        }
    }

    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(MEMORY_FILE), memoryMap);
        } catch (Exception e) {
            System.err.println("保存记忆失败: " + e.getMessage());
        }
    }

    public void put(String key, String value) {
        memoryMap.put(key, value);
        save();
    }

    public String get(String key) {
        return memoryMap.get(key);
    }

    public Map<String, String> getAll() {
        return memoryMap;
    }

    public void clear() {
        memoryMap.clear();
        File f = new File(MEMORY_FILE);
        if (f.exists()) f.delete();
    }
}
