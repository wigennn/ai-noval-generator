package com.viking.ai.novel.interfaces.dto;

import lombok.Data;

/**
 * WebSocket 触发小说结构/章节大纲流式生成的请求
 */
@Data
public class NovelStreamRequest {

    /**
     * 小说 ID
     */
    private Long novelId;

    /**
     * 流式类型：structure（小说结构）或 outline（章节大纲）
     */
    private String streamType;

    /**
     * 仅当 streamType=outline 时有效：true=在已有大纲基础上继续生成，false 或不传=重新生成
     */
    private Boolean continueOutline;
}
