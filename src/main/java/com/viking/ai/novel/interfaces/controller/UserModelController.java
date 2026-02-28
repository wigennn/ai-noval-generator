package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.UserModelService;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.interfaces.aop.CheckUserId;
import com.viking.ai.novel.interfaces.aop.RequireLogin;
import com.viking.ai.novel.interfaces.dto.SaveApiSettingsRequest;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/api-settings")
    @RequireLogin
    @CheckUserId(paramIndex = 0)
    public ResponseEntity<Map<String, Object>> saveApiSettings(
            @RequestParam Long userId,
            @Valid @RequestBody SaveApiSettingsRequest request) {
        UserModel chatModel = userModelService.saveOrUpdateUserModel(
                userId,
                request.getDefaultModel(),
                request.getBaseUrl(),
                request.getChannel(),
                request.getApiKey(),
                0
        );
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
                1
        );
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API 设置保存成功");
        response.put("chatModel", chatModel);
        response.put("embeddingModel", embeddingModel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api-settings")
    @RequireLogin
    @CheckUserId(paramIndex = 0)
    public ResponseEntity<Map<String, Object>> getApiSettings(@RequestParam Long userId) {
        UserModel chatModel = userModelService.getUserModel(userId, 0).orElse(null);
        UserModel embeddingModel = userModelService.getUserModel(userId, 1).orElse(null);
        Map<String, Object> response = new HashMap<>();
        response.put("chatModel", chatModel);
        response.put("embeddingModel", embeddingModel);
        return ResponseEntity.ok(response);
    }
}
