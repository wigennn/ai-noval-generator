package com.viking.ai.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNovelRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String genre;
    
    private String settingText;
}
