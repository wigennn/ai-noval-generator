package com.viking.ai.novel.domain.repository;

import com.viking.ai.novel.domain.model.UserModel;

import java.util.List;
import java.util.Optional;

public interface UserModelRepository {
    UserModel save(UserModel userModel);
    Optional<UserModel> findById(Long id);
    List<UserModel> findByUserId(Long userId);
    Optional<UserModel> findByUserIdAndType(Long userId, Integer type);
}
