package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserModelRepository extends JpaRepository<UserModel, Long> {
    List<UserModel> findByUserId(Long userId);
    Optional<UserModel> findByUserIdAndType(Long userId, Integer type);
}
