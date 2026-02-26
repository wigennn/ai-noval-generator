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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 章节生成任务服务（异步处理）
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
     * 异步生成章节内容
     */
    @Async("taskExecutor")
    @Transactional
    public void generateChapterContentAsync(Long novelId, Long chapterId, Long taskId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        
        // 更新任务状态为处理中
        task.setTaskStatus(1);
        taskRepository.save(task);
        
        chapter.setStatus(1); // 处理中
        chapterRepository.save(chapter);
        
        try {
            // 获取前几章的摘要作为上下文
            List<Chapter> previousChapters = chapterRepository.findByNovelId(novel.getId())
                    .stream()
                    .filter(c -> c.getChapterNumber() < chapter.getChapterNumber())
                    .sorted((a, b) -> Integer.compare(a.getChapterNumber(), b.getChapterNumber()))
                    .collect(Collectors.toList());
            
            List<String> previousAbstracts = previousChapters.stream()
                    .map(Chapter::getAbstractContent)
                    .filter(abstractContent -> abstractContent != null && !abstractContent.isEmpty())
                    .collect(Collectors.toList());
            
            // 调用 AI 生成章节内容
            String content = aiModelService.generateChapterContent(
                    novel.getTitle(),
                    novel.getGenre(),
                    novel.getSettingText(),
                    novel.getStructure(),
                    chapter.getTitle(),
                    chapter.getAbstractContent(),
                    previousAbstracts
            );
            
            // 更新章节内容
            chapter.setContent(content);
            
            // 如果摘要为空，生成摘要
            if (chapter.getAbstractContent() == null || chapter.getAbstractContent().isEmpty()) {
                String abstractContent = aiModelService.generateChapterAbstract(content);
                chapter.setAbstractContent(abstractContent);
            }
            
            // 存储到向量数据库
            String vectorId = qdrantService.storeChapter(chapter.getId().toString(), content);
            chapter.setVectorId(vectorId);
            
            chapter.setStatus(2); // 处理完成
            chapterRepository.save(chapter);
            
            // 更新任务状态为完成
            task.setTaskStatus(2);
            taskRepository.save(task);
            
            log.info("Successfully generated chapter {} for novel: {}", chapter.getChapterNumber(), novel.getId());
        } catch (Exception e) {
            log.error("Error generating chapter content for chapter: {}", chapter.getId(), e);
            chapter.setStatus(0); // 重置为待处理
            chapterRepository.save(chapter);
            task.setTaskStatus(0); // 重置为待处理
            taskRepository.save(task);
        }
    }
}
