package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.UserModelService;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.interfaces.dto.SaveApiSettingsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user-models")
@RequiredArgsConstructor
@Slf4j
public class UserModelController {
    
    private final UserModelService userModelService;
    
    /**
     * 保存 API 设置（对话模型和嵌入模型）
     */
    @PostMapping("/api-settings")
    public ResponseEntity<Map<String, Object>> saveApiSettings(
            @RequestParam Long userId,
            @Valid @RequestBody SaveApiSettingsRequest request) {
        try {
            // 保存对话模型配置（type = 0）
            UserModel chatModel = userModelService.saveOrUpdateUserModel(
                    userId,
                    request.getDefaultModel(),
                    request.getBaseUrl(),
                    request.getChannel(),
                    request.getApiKey(),
                    0 // 对话模型
            );
            
            // 保存嵌入模型配置（type = 1）
            // 如果嵌入模型 API Key 为空，使用对话模型的 API Key
            String embeddingApiKey = request.getEmbeddingApiKey();
            if (embeddingApiKey == null || embeddingApiKey.trim().isEmpty()) {
                embeddingApiKey = request.getApiKey();
            }
            
            UserModel embeddingModel = userModelService.saveOrUpdateUserModel(
                    userId,
                    request.getEmbeddingModel(),
                    request.getEmbeddingBaseUrl(),
                    request.getChannel(),
                    embeddingApiKey,
                    1 // 嵌入模型
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "API 设置保存成功");
            response.put("chatModel", chatModel);
            response.put("embeddingModel", embeddingModel);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving API settings", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取用户的 API 设置
     */
    @GetMapping("/api-settings")
    public ResponseEntity<Map<String, Object>> getApiSettings(@RequestParam Long userId) {
        try {
            UserModel chatModel = userModelService.getUserModel(userId, 0).orElse(null);
            UserModel embeddingModel = userModelService.getUserModel(userId, 1).orElse(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("chatModel", chatModel);
            response.put("embeddingModel", embeddingModel);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting API settings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
