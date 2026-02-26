package com.viking.ai.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadUserVectorRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "资料库名称不能为空")
    private String vectorName;

    @NotBlank(message = "资料内容不能为空")
    private String content;
}
