package com.viking.ai.novel.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户资料库（用户上传的文档等对应的向量记录）
 */
@Entity
@Table(name = "user_vectors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vector_name", nullable = false)
    private String vectorName;

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
