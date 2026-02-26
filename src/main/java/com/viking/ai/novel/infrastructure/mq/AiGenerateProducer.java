package com.viking.ai.novel.infrastructure.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AI 生成任务 MQ 生产者：发送小说结构、章节内容生成任务
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true")
public class AiGenerateProducer {

    public static final String TOPIC_NOVEL_STRUCTURE = "ai-novel-structure-generate";
    public static final String TOPIC_CHAPTER_CONTENT = "ai-novel-chapter-generate";

    private final RocketMQTemplate rocketMQTemplate;

    public void sendNovelStructure(Long novelId, Long taskId) {
        NovelStructureMessage msg = new NovelStructureMessage(novelId, taskId);
        rocketMQTemplate.convertAndSend(TOPIC_NOVEL_STRUCTURE, msg);
        log.info("Sent novel structure task: novelId={}, taskId={}", novelId, taskId);
    }

    public void sendChapterContent(Long novelId, Long chapterId, Long taskId) {
        ChapterContentMessage msg = new ChapterContentMessage(novelId, chapterId, taskId);
        rocketMQTemplate.convertAndSend(TOPIC_CHAPTER_CONTENT, msg);
        log.info("Sent chapter content task: novelId={}, chapterId={}, taskId={}", novelId, chapterId, taskId);
    }
}
