package com.viking.ai.novel.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "model_name", nullable = false)
    private String modelName;
    
    @Column(name = "model_url", nullable = false)
    private String modelUrl;
    
    @Column(name = "model_type", nullable = false)
    private String modelType;
    
    @Column(name = "api_key", nullable = false)
    private String apiKey;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
