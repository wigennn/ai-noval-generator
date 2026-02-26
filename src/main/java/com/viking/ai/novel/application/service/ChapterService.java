package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 章节应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterService {
    
    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final ChapterGenerationTaskService chapterGenerationTaskService;
    private final QdrantService qdrantService;
    
    /**
     * 生成章节
     */
    @Transactional
    public Chapter generateChapter(Long novelId, Integer chapterNumber, String chapterTitle, String chapterAbstract) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        
        // 检查章节是否已存在
        Optional<Chapter> existingChapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (existingChapter.isPresent()) {
            throw new RuntimeException("Chapter already exists: " + chapterNumber);
        }
        
        // 创建章节实体
        Chapter chapter = Chapter.builder()
                .novelId(novelId)
                .chapterNumber(chapterNumber)
                .title(chapterTitle)
                .abstractContent(chapterAbstract)
                .status(0) // 待处理
                .build();
        
        chapter = chapterRepository.save(chapter);
        
        // 创建异步任务生成章节内容
        Task task = Task.builder()
                .taskName("生成章节内容")
                .taskType("GENERATE_CHAPTER")
                .taskRelationId(chapter.getId())
                .taskStatus(0) // 待处理
                .build();
        taskRepository.save(task);
        
        // 异步生成章节内容
        chapterGenerationTaskService.generateChapterContentAsync(novel.getId(), chapter.getId(), task.getId());
        
        return chapter;
    }
    
    /**
     * 根据ID获取章节
     */
    public Optional<Chapter> getChapterById(Long id) {
        return chapterRepository.findById(id);
    }
    
    /**
     * 获取小说的所有章节
     */
    public List<Chapter> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findByNovelId(novelId);
    }
    
    /**
     * 更新章节
     */
    @Transactional
    public Chapter updateChapter(Long id, String title, String content) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));
        
        if (title != null) {
            chapter.setTitle(title);
        }
        if (content != null) {
            chapter.setContent(content);
            // 更新向量数据库
            if (chapter.getVectorId() != null) {
                qdrantService.deleteVector(chapter.getVectorId());
            }
            String vectorId = qdrantService.storeChapter(chapter.getId().toString(), content);
            chapter.setVectorId(vectorId);
        }
        
        return chapterRepository.save(chapter);
    }
    
    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));
        
        // 删除向量数据库中的内容
        if (chapter.getVectorId() != null) {
            qdrantService.deleteVector(chapter.getVectorId());
        }
        
        chapterRepository.deleteById(id);
    }
}
