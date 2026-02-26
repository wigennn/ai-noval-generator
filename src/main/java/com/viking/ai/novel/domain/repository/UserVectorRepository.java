package com.viking.ai.novel.domain.repository;

import com.viking.ai.novel.domain.model.UserVector;

import java.util.List;
import java.util.Optional;

public interface UserVectorRepository {
    UserVector save(UserVector userVector);
    Optional<UserVector> findById(Long id);
    List<UserVector> findByUserId(Long userId);
    void deleteById(Long id);
}
