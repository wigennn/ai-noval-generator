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
public class NovelDTO {
    private Long id;
    private Long userId;
    private String title;
    private String genre;
    private String settingText;
    private String structure;
    private Integer status;
    private LocalDateTime createdAt;
}
