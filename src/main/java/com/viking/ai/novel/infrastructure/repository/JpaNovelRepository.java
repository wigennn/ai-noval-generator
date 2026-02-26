package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaNovelRepository extends JpaRepository<Novel, Long> {
    List<Novel> findByUserId(Long userId);
}
