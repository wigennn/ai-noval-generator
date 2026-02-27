package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChapterRepositoryImpl implements ChapterRepository {
    private final JpaChapterRepository jpaChapterRepository;

    @Override
    public Chapter save(Chapter chapter) {
        return jpaChapterRepository.save(chapter);
    }

    @Override
    public Optional<Chapter> findById(Long id) {
        return jpaChapterRepository.findById(id);
    }

    @Override
    public List<Chapter> findByNovelId(Long novelId) {
        return jpaChapterRepository.findByNovelId(novelId);
    }

    @Override
    public Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber) {
        return jpaChapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
    }

    @Override
    public void deleteById(Long id) {
        jpaChapterRepository.deleteById(id);
    }

    @Override
    public void deleteByNovelId(Long novelId) {
        jpaChapterRepository.deleteByNovelId(novelId);
    }
}
