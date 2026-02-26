package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小说生成任务服务（异步处理）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NovelGenerationTaskService {
    
    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final AiModelService aiModelService;
    
    /**
     * 异步生成小说结构
     */
    @Async("taskExecutor")
    @Transactional
    public void generateNovelStructureAsync(Long novelId, Long taskId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        
        // 更新任务状态为处理中
        task.setTaskStatus(1);
        taskRepository.save(task);
        
        try {
            // 调用 AI 生成小说结构
            String structure = aiModelService.generateNovelStructure(
                    novel.getTitle(),
                    novel.getGenre(),
                    novel.getSettingText()
            );
            
            // 更新小说结构
            novel.setStructure(structure);
            novelRepository.save(novel);
            
            // 更新任务状态为完成
            task.setTaskStatus(2);
            taskRepository.save(task);
            
            log.info("Successfully generated structure for novel: {}", novel.getId());
        } catch (Exception e) {
            log.error("Error generating novel structure for novel: {}", novel.getId(), e);
            task.setTaskStatus(0); // 重置为待处理，可以重试
            taskRepository.save(task);
        }
    }
}
