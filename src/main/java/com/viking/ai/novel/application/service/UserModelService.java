package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户模型应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserModelService {
    
    private final UserModelRepository userModelRepository;
    
    /**
     * 保存或更新用户模型配置
     * @param userId 用户ID
     * @param modelName 模型名称
     * @param modelUrl 模型URL
     * @param modelType 模型类型（如 DeepSeek, OpenAI 等）
     * @param apiKey API密钥
     * @param type 类型：0=对话模型，1=嵌入模型
     * @return 保存后的 UserModel
     */
    @Transactional
    public UserModel saveOrUpdateUserModel(Long userId, String modelName, String modelUrl, 
                                          String modelType, String apiKey, Integer type) {
        // 查找是否已存在该用户的该类型模型配置
        Optional<UserModel> existing = userModelRepository.findByUserIdAndType(userId, type);
        
        UserModel userModel;
        if (existing.isPresent()) {
            // 更新现有配置
            userModel = existing.get();
            userModel.setModelName(modelName);
            userModel.setModelUrl(modelUrl);
            userModel.setModelType(modelType);
            userModel.setApiKey(apiKey);
            log.info("Updating user model config: userId={}, type={}", userId, type);
        } else {
            // 创建新配置
            userModel = UserModel.builder()
                    .userId(userId)
                    .modelName(modelName)
                    .modelUrl(modelUrl)
                    .modelType(modelType)
                    .apiKey(apiKey)
                    .type(type)
                    .build();
            log.info("Creating new user model config: userId={}, type={}", userId, type);
        }
        
        return userModelRepository.save(userModel);
    }
    
    /**
     * 获取用户的模型配置
     * @param userId 用户ID
     * @param type 类型：0=对话模型，1=嵌入模型
     * @return 模型配置
     */
    public Optional<UserModel> getUserModel(Long userId, Integer type) {
        return userModelRepository.findByUserIdAndType(userId, type);
    }
    
    /**
     * 获取用户的所有模型配置
     * @param userId 用户ID
     * @return 模型配置列表
     */
    public java.util.List<UserModel> getUserModels(Long userId) {
        return userModelRepository.findByUserId(userId);
    }
}
