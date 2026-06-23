package com.soulmate.memory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工程化的实体提取器，使用编译后的正则从用户输入中提取偏好信息。
 * 所有模式集中在静态常量中便于维护。
 */
public class MemoryExtractor {

    private static final Pattern[] PATTERNS = {
        Pattern.compile("我(?:也)?喜欢(.*?)(?:[。，！？,.!?]|$)"),
        Pattern.compile("我(?:也)?讨厌(.*?)(?:[。，！？,.!?]|$)"),
        Pattern.compile("我(?:名字|叫)(?:是)?(.*?)(?:[。，！？,.!?]|$)"),
        Pattern.compile("我(?:住在?|来自)(.*?)(?:[。，！？,.!?]|$)"),
        Pattern.compile("我(?:今年|已经)?(\\d+)\\s*岁"),
        Pattern.compile("我(?:做|是|从事)(.*?)(?:[。，！？,.!?]|$)"),
    };

    private static final String[] KEYS = {"喜欢", "讨厌", "名字", "住址", "年龄", "职业"};

    /**
     * 从用户输入中提取实体并存入记忆仓库。
     */
    public static void extractAndSave(String input, MemoryRepository memory) {
        if (input == null || input.isBlank()) return;

        for (int i = 0; i < PATTERNS.length; i++) {
            Matcher matcher = PATTERNS[i].matcher(input);
            if (matcher.find()) {
                String value = matcher.group(1).trim();
                if (!value.isEmpty() && memory.get(KEYS[i]) == null) {
                    memory.put(KEYS[i], value);
                }
            }
        }
    }
}
