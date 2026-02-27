package com.viking.ai.novel.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_name", nullable = false)
    private String taskName;
    
    @Column(name = "task_type", nullable = false)
    private String taskType;
    
    @Column(name = "task_relation_id", nullable = false)
    private Long taskRelationId;
    
    @Column(name = "task_status")
    @Builder.Default
    private Integer taskStatus = 0; // 0: 待处理，1: 处理中，2: 处理完成 3-处理失败
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
