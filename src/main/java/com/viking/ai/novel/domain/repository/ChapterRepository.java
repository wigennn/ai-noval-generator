package com.viking.ai.novel.domain.repository;

import com.viking.ai.novel.domain.model.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository {
    Chapter save(Chapter chapter);
    Optional<Chapter> findById(Long id);
    List<Chapter> findByNovelId(Long novelId);
    Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);
    void deleteById(Long id);
    void deleteByNovelId(Long novelId);
}
