package com.viking.ai.novel.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 推送给前端的章节流式生成消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterStreamPayload {

    /**
     * 消息类型：delta / complete / error
     */
    private String type;

    /**
     * 文本内容（type=delta 时为增量内容；type=error 时为错误信息）
     */
    private String content;
}

