package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.model.UserVector;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.domain.repository.UserVectorRepository;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户向量（资料库）应用服务
 */
@Service
@RequiredArgsConstructor
public class UserVectorService {

    private final UserVectorRepository userVectorRepository;
    private final QdrantService qdrantService;
    private final UserModelRepository userModelRepository;

    @Transactional
    public UserVector create(Long userId, String vectorName, String vectorId) {
        UserVector entity = UserVector.builder()
                .userId(userId)
                .vectorName(vectorName)
                .vectorId(vectorId)
                .build();
        return userVectorRepository.save(entity);
    }

    /**
     * 上传资料内容：写入向量库后创建资料库记录
     */
    @Transactional
    public UserVector createFromContent(Long userId, String vectorName, String content) {
        UserModel model = userModelRepository.findByUserIdAndType(userId, ModelTypeEnum.VECTOR.getType())
                .orElseThrow(() -> new RuntimeException("User model not found: " + userId));
        String vectorId = qdrantService.storeText(content, model);
        return create(userId, vectorName, vectorId);
    }

    public Optional<UserVector> getById(Long id) {
        return userVectorRepository.findById(id);
    }

    public List<UserVector> listByUserId(Long userId) {
        return userVectorRepository.findByUserId(userId);
    }

    @Transactional
    public void delete(Long id) {
        userVectorRepository.deleteById(id);
    }
}
