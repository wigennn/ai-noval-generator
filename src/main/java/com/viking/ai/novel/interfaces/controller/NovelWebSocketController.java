package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import com.viking.ai.novel.interfaces.dto.ChapterStreamPayload;
import com.viking.ai.novel.interfaces.dto.NovelStreamRequest;
import com.viking.ai.novel.interfaces.dto.StopStreamRequest;
import com.viking.ai.novel.interfaces.strategy.NovelStreamStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 小说结构和章节大纲 WebSocket 流式生成
 * <p>
 * 客户端：
 * - 连接 /ws
 * - 订阅 /topic/novels/{novelId}/structure 或 /topic/novels/{novelId}/outline
 * - 发送消息到 /app/novels/stream 触发生成
 * - 发送消息到 /app/novels/stop 停止生成
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class NovelWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NovelRepository novelRepository;
    private final UserModelRepository userModelRepository;
    private final List<NovelStreamStrategy> streamStrategies;

    // 存储正在进行的流式生成任务，key: "novelId:streamType"
    private final ConcurrentHashMap<String, AtomicBoolean> activeStreams = new ConcurrentHashMap<>();

    @MessageMapping("/novels/stream")
    public void streamNovel(NovelStreamRequest request) {
        if (request.getNovelId() == null || request.getStreamType() == null) {
            log.warn("Invalid novel stream request: {}", request);
            return;
        }

        Long novelId = request.getNovelId();
        String streamType = request.getStreamType();

        NovelStreamStrategy strategy = getStrategy(streamType);
        if (strategy == null) {
            log.warn("Unknown stream type: {}", streamType);
            return;
        }

        String destination = strategy.buildDestination(novelId);

        // 创建停止标记
        String streamKey = String.format("%d:%s", novelId, streamType);
        AtomicBoolean stopped = new AtomicBoolean(false);
        activeStreams.put(streamKey, stopped);

        try {
            Novel novel = novelRepository.findById(novelId)
                    .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
            UserModel model = userModelRepository.findByUserIdAndType(
                            novel.getUserId(), ModelTypeEnum.NORMAL.getType())
                    .orElseThrow(() -> new RuntimeException("User model not found: " + novel.getUserId()));

            strategy.stream(novel, model, messagingTemplate, stopped, () -> activeStreams.remove(streamKey));
        } catch (Exception e) {
            activeStreams.remove(streamKey);
            log.error("Error starting novel stream", e);
            messagingTemplate.convertAndSend(destination,
                    new ChapterStreamPayload("error", e.getMessage()));
        }
    }

    @MessageMapping("/novels/stop")
    public void stopNovelStream(StopStreamRequest request) {
        if (request.getNovelId() == null || request.getStreamType() == null) {
            log.warn("Invalid stop stream request: {}", request);
            return;
        }

        String streamKey = String.format("%d:%s", request.getNovelId(), request.getStreamType());
        AtomicBoolean stopped = activeStreams.get(streamKey);
        
        if (stopped != null) {
            stopped.set(true);
            log.info("Stopped stream: {}", streamKey);
        } else {
            log.warn("Stream not found: {}", streamKey);
        }
    }

    private NovelStreamStrategy getStrategy(String type) {
        if (type == null) {
            return null;
        }
        return streamStrategies.stream()
                .filter(s -> type.equals(s.getType()))
                .findFirst()
                .orElse(null);
    }
}
