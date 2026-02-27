package com.viking.ai.novel.interfaces.strategy;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.interfaces.dto.ChapterStreamPayload;
import com.viking.ai.novel.interfaces.dto.NovelStreamRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
/**
 * 小说结构流式生成策略
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StructureStreamStrategy implements NovelStreamStrategy {

    private final AiModelService aiModelService;
    private final NovelRepository novelRepository;

    @Override
    public String getType() {
        return "structure";
    }

    @Override
    public String buildDestination(Long novelId) {
        return String.format("/topic/novels/%d/structure", novelId);
    }

    @Override
    public void stream(Novel novel,
                       UserModel model,
                       SimpMessagingTemplate messagingTemplate,
                       AtomicBoolean stopped,
                       NovelStreamRequest request,
                       Runnable onFinished) {
        String destination = buildDestination(novel.getId());

        aiModelService.streamNovelStructure(
                novel.getTitle(),
                novel.getGenre(),
                novel.getSettingText(),
                novel.getChapterNumber(),
                model,
                new AiModelService.StreamCallback() {
                    @Override
                    public void onDelta(String text) {
                        if (stopped.get()) {
                            return;
                        }
                        messagingTemplate.convertAndSend(destination,
                                new ChapterStreamPayload("delta", text));
                    }

                    @Override
                    public void onComplete(String fullText) {
                        if (onFinished != null) {
                            onFinished.run();
                        }
                        if (stopped.get()) {
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("stopped", null));
                            return;
                        }
                        try {
                            // 生成完成，保存小说结构
                            novel.setStructure(fullText);
                            // novel 的保存由调用方负责或在此处完成
                            novelRepository.save(novel);
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("complete", null));
                        } catch (Exception e) {
                            log.error("Error finishing streamed novel structure {}", novel.getId(), e);
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("error", e.getMessage()));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (onFinished != null) {
                            onFinished.run();
                        }
                        log.error("Error streaming novel structure", t);
                        messagingTemplate.convertAndSend(destination,
                                new ChapterStreamPayload("error", t.getMessage()));
                    }
                }
        );
    }
}

