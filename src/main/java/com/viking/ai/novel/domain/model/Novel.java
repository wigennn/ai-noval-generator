package com.viking.ai.novel.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "novel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "genre", length = 50)
    private String genre;
    
    @Column(name = "setting_text", columnDefinition = "LONGTEXT")
    private String settingText;
    
    @Column(name = "structure", columnDefinition = "LONGTEXT")
    private String structure;
    
    @Column(name = "status")
    @Builder.Default
    private Integer status = 0; // 0: 草稿，1: 发布中，2: 已完成
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
