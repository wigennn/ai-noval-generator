package com.viking.ai.novel.interfaces.strategy;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 小说流式生成策略（结构 / 章节大纲）
 */
public interface NovelStreamStrategy {

    /**
     * 策略类型：structure / outline
     */
    String getType();

    /**
     * 构建 WebSocket 目的地
     */
    String buildDestination(Long novelId);

    /**
     * 执行流式生成
     *
     * @param novel        小说实体
     * @param model        用户模型配置
     * @param messagingTemplate WebSocket 消息模板
     * @param stopped      停止标记
     * @param onFinished   生成完成或出错时的回调（用于清理资源等）
     */
    void stream(Novel novel,
                UserModel model,
                SimpMessagingTemplate messagingTemplate,
                AtomicBoolean stopped,
                Runnable onFinished);
}

