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
public class ChapterDTO {
    private Long id;
    private Long novelId;
    private Integer chapterNumber;
    private String title;
    private String abstractContent;
    private String content;
    private String vectorId;
    private Integer status;
    private LocalDateTime createdAt;
}
