package com.viking.ai.novel.interfaces.dto;

import lombok.Data;

/**
 * WebSocket 触发章节流式生成的请求
 */
@Data
public class ChapterStreamRequest {

    /**
     * 小说 ID
     */
    private Long novelId;

    /**
     * 章节序号（第几章）
     */
    private Integer chapterNumber;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节摘要（可选）
     */
    private String abstractContent;
}

