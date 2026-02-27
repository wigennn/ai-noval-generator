package com.viking.ai.novel.infrastructure.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 章节大纲生成 MQ 消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterOutlineMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long novelId;
    private Long taskId;
}
