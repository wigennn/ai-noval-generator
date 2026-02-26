package com.viking.ai.novel.domain.repository;

import com.viking.ai.novel.domain.model.NovelVector;

import java.util.List;
import java.util.Optional;

public interface NovelVectorRepository {
    NovelVector save(NovelVector novelVector);
    Optional<NovelVector> findById(Long id);
    List<NovelVector> findByNovelId(Long novelId);
    void deleteById(Long id);
}
