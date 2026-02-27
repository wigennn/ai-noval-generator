package com.viking.ai.novel.interfaces.strategy;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.interfaces.dto.ChapterStreamPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 章节大纲流式生成策略
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutlineStreamStrategy implements NovelStreamStrategy {

    private final AiModelService aiModelService;
    private final NovelRepository novelRepository;
    private final ChapterService chapterService;

    @Override
    public String getType() {
        return "outline";
    }

    @Override
    public String buildDestination(Long novelId) {
        return String.format("/topic/novels/%d/outline", novelId);
    }

    @Override
    public void stream(Novel novel,
                       UserModel model,
                       SimpMessagingTemplate messagingTemplate,
                       AtomicBoolean stopped,
                       Runnable onFinished) {
        String destination = buildDestination(novel.getId());

        if (novel.getStructure() == null || novel.getStructure().isEmpty()) {
            messagingTemplate.convertAndSend(destination,
                    new ChapterStreamPayload("error", "请先完成小说架构的生成"));
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        // 如果已存在章节大纲，说明是"重新生成"，需要先删除原有章节记录
        if (novel.getChapterOutline() != null && !novel.getChapterOutline().isEmpty()) {
            Long novelId = novel.getId();
            log.info("Regenerating chapter outline, delete existing chapters for novel: {}", novelId);
            chapterService.deleteChaptersByNovelId(novelId);
        }

        aiModelService.streamChapterOutline(
                novel.getTitle(),
                novel.getGenre(),
                novel.getSettingText(),
                novel.getStructure(),
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
                            // 生成完成，保存章节大纲
                            novel.setChapterOutline(fullText);
                            novelRepository.save(novel);

                            // 同步章节表：根据大纲提取每章标题，创建占位章节
                            chapterService.syncChaptersFromOutline(novel.getId(), fullText);
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("complete", null));
                        } catch (Exception e) {
                            log.error("Error finishing streamed chapter outline {}", novel.getId(), e);
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("error", e.getMessage()));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (onFinished != null) {
                            onFinished.run();
                        }
                        log.error("Error streaming chapter outline", t);
                        messagingTemplate.convertAndSend(destination,
                                new ChapterStreamPayload("error", t.getMessage()));
                    }
                }
        );
    }
}

