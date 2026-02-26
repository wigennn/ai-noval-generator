package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.NovelVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaNovelVectorRepository extends JpaRepository<NovelVector, Long> {
    List<NovelVector> findByNovelId(Long novelId);
}
