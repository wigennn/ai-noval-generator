package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小说结构生成：核心逻辑供同步调用或 MQ 消费者调用
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NovelGenerationTaskService {

    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final AiModelService aiModelService;
    private final UserModelRepository userModelRepository;

    /**
     * 执行小说结构生成（同步逻辑，供实时调用或 MQ 消费者调用）
     */
    @Transactional
    public void doGenerateNovelStructure(Long novelId, Long taskId, Long userId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        UserModel model = userModelRepository.findByUserIdAndType(userId, ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("User model not found: " + userId));

        task.setTaskStatus(1);
        taskRepository.save(task);

        try {
            String structure = aiModelService.generateNovelStructure(
                    novel.getTitle(),
                    novel.getGenre(),
                    novel.getSettingText(),
                    novel.getChapterNumber(),
                    model
            );
            novel.setStructure(structure);
            novelRepository.save(novel);
            task.setTaskStatus(2);
            taskRepository.save(task);
            log.info("Successfully generated structure for novel: {}", novel.getId());
        } catch (Exception e) {
            log.error("Error generating novel structure for novel: {}", novel.getId(), e);
            task.setTaskStatus(3);
            taskRepository.save(task);
        }
    }
    
    /**
     * 执行章节大纲生成（同步逻辑，供实时调用或 MQ 消费者调用）
     */
    @Transactional
    public void doGenerateChapterOutline(Long novelId, Long taskId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        UserModel model = userModelRepository.findByUserIdAndType(novel.getUserId(), ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("User model not found: " + novel.getUserId()));
        
        if (novel.getStructure() == null || novel.getStructure().isEmpty()) {
            log.error("Cannot generate chapter outline: novel structure is empty for novel: {}", novelId);
            task.setTaskStatus(0);
            taskRepository.save(task);
            return;
        }
        
        task.setTaskStatus(1);
        taskRepository.save(task);
        
        try {
            String chapterOutline = aiModelService.generateChapterOutline(
                    novel.getTitle(),
                    novel.getGenre(),
                    novel.getSettingText(),
                    novel.getStructure(),
                    model
            );
            novel.setChapterOutline(chapterOutline);
            novelRepository.save(novel);
            task.setTaskStatus(2);
            taskRepository.save(task);
            log.info("Successfully generated chapter outline for novel: {}", novel.getId());
        } catch (Exception e) {
            log.error("Error generating chapter outline for novel: {}", novel.getId(), e);
            task.setTaskStatus(3);
            taskRepository.save(task);
        }
    }
}
