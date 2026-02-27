package com.viking.ai.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveApiSettingsRequest {
    @NotBlank(message = "渠道不能为空")
    private String channel;
    
    @NotBlank(message = "API Base URL 不能为空")
    private String baseUrl;
    
    @NotBlank(message = "API Key 不能为空")
    private String apiKey;
    
    @NotBlank(message = "默认模型不能为空")
    private String defaultModel;
    
    @NotBlank(message = "嵌入模型 API Base URL 不能为空")
    private String embeddingBaseUrl;
    
    // 可选，留空则使用对话模型的 Key
    private String embeddingApiKey;
    
    @NotBlank(message = "嵌入模型名称不能为空")
    private String embeddingModel;
}
