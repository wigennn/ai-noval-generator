package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {
    private final JpaTaskRepository jpaTaskRepository;

    @Override
    public Task save(Task task) {
        return jpaTaskRepository.save(task);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return jpaTaskRepository.findById(id);
    }

    @Override
    public List<Task> findByTaskTypeAndTaskStatus(String taskType, Integer taskStatus) {
        return jpaTaskRepository.findByTaskTypeAndTaskStatus(taskType, taskStatus);
    }

    @Override
    public List<Task> findByTaskRelationId(Long taskRelationId) {
        return jpaTaskRepository.findByTaskRelationId(taskRelationId);
    }
}
