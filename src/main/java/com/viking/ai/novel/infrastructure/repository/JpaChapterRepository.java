package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByNovelId(Long novelId);
    Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);
    void deleteByNovelId(Long novelId);
}
