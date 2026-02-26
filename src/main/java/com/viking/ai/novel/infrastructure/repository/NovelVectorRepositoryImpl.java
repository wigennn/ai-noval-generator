package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.NovelVector;
import com.viking.ai.novel.domain.repository.NovelVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NovelVectorRepositoryImpl implements NovelVectorRepository {
    private final JpaNovelVectorRepository jpaNovelVectorRepository;

    @Override
    public NovelVector save(NovelVector novelVector) {
        return jpaNovelVectorRepository.save(novelVector);
    }

    @Override
    public Optional<NovelVector> findById(Long id) {
        return jpaNovelVectorRepository.findById(id);
    }

    @Override
    public List<NovelVector> findByNovelId(Long novelId) {
        return jpaNovelVectorRepository.findByNovelId(novelId);
    }

    @Override
    public void deleteById(Long id) {
        jpaNovelVectorRepository.deleteById(id);
    }
}
