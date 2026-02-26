package com.viking.ai.novel.domain.repository;

import com.viking.ai.novel.domain.model.Novel;

import java.util.List;
import java.util.Optional;

public interface NovelRepository {
    Novel save(Novel novel);
    Optional<Novel> findById(Long id);
    List<Novel> findByUserId(Long userId);
    void deleteById(Long id);
}
