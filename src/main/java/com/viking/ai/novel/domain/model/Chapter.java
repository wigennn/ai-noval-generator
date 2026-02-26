package com.viking.ai.novel.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chapters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "abstract_content", length = 500)
    private String abstractContent;
    
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(name = "vector_id", length = 100)
    private String vectorId;
    
    @Column(name = "status")
    @Builder.Default
    private Integer status = 0; // 0: 待处理，1: 处理中，2: 处理完成
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
