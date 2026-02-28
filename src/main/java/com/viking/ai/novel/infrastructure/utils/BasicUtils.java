package com.viking.ai.novel.infrastructure.utils;

public class BasicUtils {
    public static String getCollectionName(Long userId, Long novelId) {
        return String.format("novel-chapters-%s-%s", userId, novelId);
    }
}
