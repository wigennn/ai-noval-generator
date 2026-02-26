package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NovelRepositoryImpl implements NovelRepository {
    private final JpaNovelRepository jpaNovelRepository;

    @Override
    public Novel save(Novel novel) {
        return jpaNovelRepository.save(novel);
    }

    @Override
    public Optional<Novel> findById(Long id) {
        return jpaNovelRepository.findById(id);
    }

    @Override
    public List<Novel> findByUserId(Long userId) {
        return jpaNovelRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaNovelRepository.deleteById(id);
    }
}
