package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.NovelVector;
import com.viking.ai.novel.domain.repository.NovelVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 小说向量关联应用服务
 */
@Service
@RequiredArgsConstructor
public class NovelVectorService {

    private final NovelVectorRepository novelVectorRepository;

    @Transactional
    public NovelVector create(Long novelId, String vectorId) {
        NovelVector entity = NovelVector.builder()
                .novelId(novelId)
                .vectorId(vectorId)
                .build();
        return novelVectorRepository.save(entity);
    }

    public Optional<NovelVector> getById(Long id) {
        return novelVectorRepository.findById(id);
    }

    public List<NovelVector> listByNovelId(Long novelId) {
        return novelVectorRepository.findByNovelId(novelId);
    }

    @Transactional
    public void delete(Long id) {
        novelVectorRepository.deleteById(id);
    }
}
