package com.viking.ai.novel.application.service;

import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.application.service.NovelVectorService;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.infrastructure.mq.AiGenerateProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 小说应用服务
 */
@Service
@Slf4j
public class NovelService {

    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final NovelGenerationTaskService novelGenerationTaskService;
    private final AiGenerateProducer aiGenerateProducer;
    private final ChapterService chapterService;
    private final NovelVectorService novelVectorService;

    public NovelService(NovelRepository novelRepository,
                        TaskRepository taskRepository,
                        NovelGenerationTaskService novelGenerationTaskService,
                        @Autowired(required = false) AiGenerateProducer aiGenerateProducer,
                        ChapterService chapterService,
                        NovelVectorService novelVectorService) {
        this.novelRepository = novelRepository;
        this.taskRepository = taskRepository;
        this.novelGenerationTaskService = novelGenerationTaskService;
        this.aiGenerateProducer = aiGenerateProducer;
        this.chapterService = chapterService;
        this.novelVectorService = novelVectorService;
    }

    /**
     * 创建小说
     */
    @Transactional
    public Novel createNovel(Long userId, String title, String genre, String settingText, 
                             Integer chapterNumber, Integer chapterWordCount) {
        Novel novel = Novel.builder()
                .userId(userId)
                .title(title)
                .genre(genre)
                .settingText(settingText)
                .chapterNumber(chapterNumber != null ? chapterNumber : 0)
                .chapterWordCount(chapterWordCount != null ? chapterWordCount : 0)
                .status(0)
                .build();
        novel = novelRepository.save(novel);
        return novel;
    }
    
    /**
     * 根据ID获取小说
     */
    public Optional<Novel> getNovelById(Long id) {
        return novelRepository.findById(id);
    }
    
    /**
     * 获取用户的所有小说
     */
    public List<Novel> getNovelsByUserId(Long userId) {
        return novelRepository.findByUserId(userId);
    }
    
    /**
     * 更新小说
     */
    @Transactional
    public Novel updateNovel(Long id, String title, String genre, String settingText,
                             Integer chapterNumber, Integer chapterWordCount) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + id));
        
        if (title != null) {
            novel.setTitle(title);
        }
        if (genre != null) {
            novel.setGenre(genre);
        }
        if (settingText != null) {
            novel.setSettingText(settingText);
        }
        if (chapterNumber != null) {
            novel.setChapterNumber(chapterNumber);
        }
        if (chapterWordCount != null) {
            novel.setChapterWordCount(chapterWordCount);
        }
        
        return novelRepository.save(novel);
    }
    
    /**
     * 删除小说（同时删除关联的章节、向量数据和小说向量关联）
     */
    @Transactional
    public void deleteNovel(Long id) {
        // 检查小说是否存在
        if (!novelRepository.findById(id).isPresent()) {
            throw new RuntimeException("Novel not found: " + id);
        }
        
        // 删除所有章节（包括向量数据）
        chapterService.deleteChaptersByNovelId(id);
        
        // 删除小说向量关联
        List<com.viking.ai.novel.domain.model.NovelVector> novelVectors = novelVectorService.listByNovelId(id);
        for (com.viking.ai.novel.domain.model.NovelVector nv : novelVectors) {
            novelVectorService.delete(nv.getId());
        }
        
        // 删除小说本身
        novelRepository.deleteById(id);
        log.info("Deleted novel {} and all related data", id);
    }
    
    /**
     * 重新生成小说架构
     * @param async true=通过 MQ 异步生成，false=同步生成（阻塞至完成）
     */
    @Transactional
    public Novel regenerateNovelStructure(Long novelId, boolean async) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        
        Task task = Task.builder()
                .taskName("重新生成小说结构")
                .taskType("GENERATE_NOVEL_STRUCTURE")
                .taskRelationId(novel.getId())
                .taskStatus(0)
                .build();
        taskRepository.save(task);
        
        if (async && aiGenerateProducer != null) {
            aiGenerateProducer.sendNovelStructure(novel.getId(), task.getId(), novel.getUserId());
        } else {
            novelGenerationTaskService.doGenerateNovelStructure(novel.getId(), task.getId(), novel.getUserId());
        }
        return novel;
    }
    
    /**
     * 生成章节大纲
     * @param async true=通过 MQ 异步生成，false=同步生成（阻塞至完成）
     */
    @Transactional
    public Novel generateChapterOutline(Long novelId, boolean async) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        
        if (novel.getStructure() == null || novel.getStructure().isEmpty()) {
            throw new RuntimeException("Novel structure is required before generating chapter outline");
        }
        
        Task task = Task.builder()
                .taskName("生成章节大纲")
                .taskType("GENERATE_CHAPTER_OUTLINE")
                .taskRelationId(novel.getId())
                .taskStatus(0)
                .build();
        taskRepository.save(task);
        
        if (async && aiGenerateProducer != null) {
            aiGenerateProducer.sendChapterOutline(novel.getId(), task.getId());
        } else {
            novelGenerationTaskService.doGenerateChapterOutline(novel.getId(), task.getId());
        }
        return novel;
    }
}
