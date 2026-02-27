package com.viking.ai.novel.interfaces.dto;

import lombok.Data;

/**
 * WebSocket 停止流式生成的请求
 */
@Data
public class StopStreamRequest {

    /**
     * 小说 ID（用于小说结构/章节大纲）
     */
    private Long novelId;

    /**
     * 章节序号（用于章节内容）
     */
    private Integer chapterNumber;

    /**
     * 流式类型：structure / outline / chapter
     */
    private String streamType;
}
