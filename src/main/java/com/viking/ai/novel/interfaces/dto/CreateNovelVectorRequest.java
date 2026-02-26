package com.viking.ai.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNovelVectorRequest {
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    @NotBlank(message = "向量ID不能为空")
    private String vectorId;
}
