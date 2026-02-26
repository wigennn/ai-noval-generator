package com.viking.ai.novel.infrastructure.mq;

import com.viking.ai.novel.application.service.ChapterGenerationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 章节内容生成 MQ 消费者：收到消息后执行 AI 生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = AiGenerateProducer.TOPIC_CHAPTER_CONTENT,
        consumerGroup = "ai-novel-chapter-consumer"
)
public class ChapterContentConsumer implements RocketMQListener<ChapterContentMessage> {

    private final ChapterGenerationTaskService chapterGenerationTaskService;

    @Override
    public void onMessage(ChapterContentMessage message) {
        log.info("Consume chapter content task: novelId={}, chapterId={}, taskId={}",
                message.getNovelId(), message.getChapterId(), message.getTaskId());
        chapterGenerationTaskService.doGenerateChapterContent(
                message.getNovelId(),
                message.getChapterId(),
                message.getTaskId()
        );
    }
}
