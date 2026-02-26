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
public class NovelVectorDTO {
    private Long id;
    private Long novelId;
    private String vectorId;
    private LocalDateTime createdAt;
}
