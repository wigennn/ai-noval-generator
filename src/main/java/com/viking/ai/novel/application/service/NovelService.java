package com.viking.ai.novel.application.service;

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

    public NovelService(NovelRepository novelRepository,
                        TaskRepository taskRepository,
                        NovelGenerationTaskService novelGenerationTaskService,
                        @Autowired(required = false) AiGenerateProducer aiGenerateProducer) {
        this.novelRepository = novelRepository;
        this.taskRepository = taskRepository;
        this.novelGenerationTaskService = novelGenerationTaskService;
        this.aiGenerateProducer = aiGenerateProducer;
    }

    /**
     * 创建小说并生成结构
     * @param async true=通过 MQ 异步生成，false=同步生成（阻塞至完成）
     */
    @Transactional
    public Novel createNovel(Long userId, String title, String genre, String settingText, boolean async) {
        Novel novel = Novel.builder()
                .userId(userId)
                .title(title)
                .genre(genre)
                .settingText(settingText)
                .status(0)
                .build();
        novel = novelRepository.save(novel);

        Task task = Task.builder()
                .taskName("生成小说结构")
                .taskType("GENERATE_NOVEL_STRUCTURE")
                .taskRelationId(novel.getId())
                .taskStatus(0)
                .build();
        taskRepository.save(task);

        if (async && aiGenerateProducer != null) {
            aiGenerateProducer.sendNovelStructure(novel.getId(), task.getId());
        } else {
            novelGenerationTaskService.doGenerateNovelStructure(novel.getId(), task.getId());
        }
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
    public Novel updateNovel(Long id, String title, String genre, String settingText) {
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
        
        return novelRepository.save(novel);
    }
    
    /**
     * 删除小说
     */
    @Transactional
    public void deleteNovel(Long id) {
        novelRepository.deleteById(id);
    }
}
