package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    /**
     * 根据关联ID获取任务列表
     */
    public List<Task> getTasksByRelationId(Long relationId) {
        return taskRepository.findByTaskRelationId(relationId);
    }
    
    /**
     * 根据任务类型和状态查询任务
     */
    public List<Task> getTasksByTypeAndStatus(String taskType, Integer taskStatus) {
        return taskRepository.findByTaskTypeAndTaskStatus(taskType, taskStatus);
    }
    
    /**
     * 获取所有进行中的任务（状态为 0 待处理 或 1 处理中）
     */
    public List<Task> getAllActiveTasks() {
        return taskRepository.findByTaskStatusIn(List.of(0, 1));
    }
}
