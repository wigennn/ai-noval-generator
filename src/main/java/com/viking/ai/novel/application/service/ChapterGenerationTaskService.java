package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 章节内容生成：核心逻辑供同步调用或 MQ 消费者调用
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterGenerationTaskService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final AiModelService aiModelService;
    private final QdrantService qdrantService;

    /**
     * 执行章节内容生成（同步逻辑，供实时调用或 MQ 消费者调用）
     */
    @Transactional
    public void doGenerateChapterContent(Long novelId, Long chapterId, Long taskId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        task.setTaskStatus(1);
        taskRepository.save(task);
        chapter.setStatus(1);
        chapterRepository.save(chapter);

        try {
            List<Chapter> previousChapters = chapterRepository.findByNovelId(novel.getId())
                    .stream()
                    .filter(c -> c.getChapterNumber() < chapter.getChapterNumber())
                    .sorted((a, b) -> Integer.compare(a.getChapterNumber(), b.getChapterNumber()))
                    .collect(Collectors.toList());
            List<String> previousAbstracts = previousChapters.stream()
                    .map(Chapter::getAbstractContent)
                    .filter(abstractContent -> abstractContent != null && !abstractContent.isEmpty())
                    .collect(Collectors.toList());

            String content = aiModelService.generateChapterContent(
                    novel.getTitle(),
                    novel.getGenre(),
                    novel.getSettingText(),
                    novel.getStructure(),
                    chapter.getTitle(),
                    chapter.getAbstractContent(),
                    previousAbstracts
            );
            chapter.setContent(content);
            if (chapter.getAbstractContent() == null || chapter.getAbstractContent().isEmpty()) {
                String abstractContent = aiModelService.generateChapterAbstract(content);
                chapter.setAbstractContent(abstractContent);
            }
            String vectorId = qdrantService.storeChapter(chapter.getId().toString(), content);
            chapter.setVectorId(vectorId);
            chapter.setStatus(2);
            chapterRepository.save(chapter);
            task.setTaskStatus(2);
            taskRepository.save(task);
            log.info("Successfully generated chapter {} for novel: {}", chapter.getChapterNumber(), novel.getId());
        } catch (Exception e) {
            log.error("Error generating chapter content for chapter: {}", chapter.getId(), e);
            chapter.setStatus(0);
            chapterRepository.save(chapter);
            task.setTaskStatus(0);
            taskRepository.save(task);
        }
    }
}
