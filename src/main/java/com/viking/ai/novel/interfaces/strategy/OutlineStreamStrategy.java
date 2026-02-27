package com.viking.ai.novel.interfaces.strategy;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.interfaces.dto.ChapterStreamPayload;
import com.viking.ai.novel.interfaces.dto.NovelStreamRequest;
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
                       NovelStreamRequest request,
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

        boolean continueOutline = Boolean.TRUE.equals(request != null ? request.getContinueOutline() : null);
        String existingOutline = (continueOutline && novel.getChapterOutline() != null && !novel.getChapterOutline().isEmpty())
                ? novel.getChapterOutline() : null;

        // 仅当「重新生成」时删除原有章节；继续生成时不删
        if (existingOutline == null && novel.getChapterOutline() != null && !novel.getChapterOutline().isEmpty()) {
            Long novelId = novel.getId();
            log.info("Regenerating chapter outline, delete existing chapters for novel: {}", novelId);
            chapterService.deleteChaptersByNovelId(novelId);
        }

        final String existingForMerge = existingOutline; // for use in onComplete

        aiModelService.streamChapterOutline(
                novel.getTitle(),
                novel.getGenre(),
                novel.getSettingText(),
                novel.getStructure(),
                existingOutline,
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
                            String fullOutline = fullText;
                            if (existingForMerge != null && !existingForMerge.isEmpty()) {
                                String continuation = fullText != null ? fullText.trim() : "";
                                fullOutline = (existingForMerge + "\n\n" + continuation).trim();
                            }
                            // 生成完成，完整更新小说表的章节大纲
                            novel.setChapterOutline(fullOutline);
                            novelRepository.save(novel);

                            // 同步章节表：根据完整大纲提取每章标题，创建/更新占位章节
                            chapterService.syncChaptersFromOutline(novel.getId(), fullOutline);
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

