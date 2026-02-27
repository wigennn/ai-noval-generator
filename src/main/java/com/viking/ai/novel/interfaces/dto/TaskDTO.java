package com.viking.ai.novel.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String taskName;
    private String taskType;
    private Long taskRelationId;
    private Integer taskStatus; // 0: 待处理，1: 处理中，2: 处理完成
    private LocalDateTime createdAt;
}
