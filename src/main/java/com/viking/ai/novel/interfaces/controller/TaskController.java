package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.TaskService;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.interfaces.dto.TaskDTO;
import com.viking.ai.novel.interfaces.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    private final TaskMapper taskMapper = TaskMapper.INSTANCE;
    
    /**
     * 获取所有进行中的任务
     */
    @GetMapping("/active")
    public ResponseEntity<List<TaskDTO>> getActiveTasks() {
        List<TaskDTO> tasks = taskService.getAllActiveTasks()
                .stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 根据关联ID获取任务列表
     */
    @GetMapping("/relation/{relationId}")
    public ResponseEntity<List<TaskDTO>> getTasksByRelationId(@PathVariable Long relationId) {
        List<TaskDTO> tasks = taskService.getTasksByRelationId(relationId)
                .stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 根据任务类型和状态查询任务
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer taskStatus) {
        List<Task> tasks;
        if (taskType != null && taskStatus != null) {
            tasks = taskService.getTasksByTypeAndStatus(taskType, taskStatus);
        } else {
            tasks = taskService.getAllActiveTasks();
        }
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDTOs);
    }
}
