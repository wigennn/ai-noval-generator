package com.viking.ai.novel.infrastructure.mq;

import com.viking.ai.novel.application.service.NovelGenerationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 小说结构生成 MQ 消费者：收到消息后执行 AI 生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = AiGenerateProducer.TOPIC_NOVEL_STRUCTURE,
        consumerGroup = "ai-novel-structure-consumer"
)
public class NovelStructureConsumer implements RocketMQListener<NovelStructureMessage> {

    private final NovelGenerationTaskService novelGenerationTaskService;

    @Override
    public void onMessage(NovelStructureMessage message) {
        log.info("Consume novel structure task: novelId={}, taskId={}", message.getNovelId(), message.getTaskId());
        novelGenerationTaskService.doGenerateNovelStructure(message.getNovelId(), message.getTaskId(), message.getUserId());
    }
}
