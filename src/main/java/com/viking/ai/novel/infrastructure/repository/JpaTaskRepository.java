package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaTaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTaskTypeAndTaskStatus(String taskType, Integer taskStatus);
    List<Task> findByTaskRelationId(Long taskRelationId);
}
