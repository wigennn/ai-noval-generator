package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.UserVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaUserVectorRepository extends JpaRepository<UserVector, Long> {
    List<UserVector> findByUserId(Long userId);
}
