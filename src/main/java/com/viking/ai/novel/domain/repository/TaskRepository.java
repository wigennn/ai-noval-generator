package com.viking.ai.novel.domain.repository;

import com.viking.ai.novel.domain.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(Long id);
    List<Task> findByTaskTypeAndTaskStatus(String taskType, Integer taskStatus);
    List<Task> findByTaskRelationId(Long taskRelationId);
    List<Task> findByTaskStatusIn(List<Integer> statuses);
}
