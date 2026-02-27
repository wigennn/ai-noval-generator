package com.viking.ai.novel.interfaces.strategy;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.interfaces.dto.ChapterStreamPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 章节大纲流式生成策略
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutlineStreamStrategy implements NovelStreamStrategy {

    private final AiModelService aiModelService;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;

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

        // 如果已存在章节大纲，说明是“重新生成”，需要先删除原有章节记录
        if (novel.getChapterOutline() != null && !novel.getChapterOutline().isEmpty()) {
            Long novelId = novel.getId();
            log.info("Regenerating chapter outline, delete existing chapters for novel: {}", novelId);
            chapterRepository.deleteByNovelId(novelId);
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
                            syncChaptersFromOutline(novel, fullText);
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

    /**
     * 从章节大纲中提取每章标题，同步到 chapters 表
     */
    private void syncChaptersFromOutline(Novel novel, String outline) {
        if (outline == null || outline.isEmpty()) {
            return;
        }
        Long novelId = novel.getId();
        // 匹配格式：## 第X章 [章节标题] 或 ## 第X章 章节标题
        Pattern pattern = Pattern.compile("^##\\s*第(\\d+)章\\s*(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(outline);

        while (matcher.find()) {
            int chapterNumber = Integer.parseInt(matcher.group(1));
            String rawTitle = matcher.group(2) != null ? matcher.group(2).trim() : "";
            // 去掉可能的方括号
            String title = rawTitle.replaceAll("^[\\[【]?|[】\\]]?$", "").trim();

            Optional<Chapter> existingOpt = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
            if (existingOpt.isPresent()) {
                // 已存在章节，不强制覆盖标题，只在标题为空时补充
                Chapter existing = existingOpt.get();
                if ((existing.getTitle() == null || existing.getTitle().isEmpty()) && !title.isEmpty()) {
                    existing.setTitle(title);
                    chapterRepository.save(existing);
                }
            } else {
                // 创建占位章节记录（仅有编号和标题，状态为待处理）
                Chapter chapter = Chapter.builder()
                        .novelId(novelId)
                        .chapterNumber(chapterNumber)
                        .title(title.isEmpty() ? null : title)
                        .status(0)
                        .build();
                chapterRepository.save(chapter);
            }
        }
    }
}

