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
public class UserVectorDTO {
    private Long id;
    private Long userId;
    private String vectorName;
    private String vectorId;
    private LocalDateTime createdAt;
}
