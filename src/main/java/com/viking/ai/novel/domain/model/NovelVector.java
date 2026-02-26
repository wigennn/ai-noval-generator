package com.viking.ai.novel.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小说向量关联表（小说设定等 RAG 向量记录）
 */
@Entity
@Table(name = "novel_vectors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelVector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    @Column(name = "vector_id", nullable = false)
    private String vectorId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
