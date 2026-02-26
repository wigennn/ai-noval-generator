package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.UserVector;
import com.viking.ai.novel.domain.repository.UserVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserVectorRepositoryImpl implements UserVectorRepository {
    private final JpaUserVectorRepository jpaUserVectorRepository;

    @Override
    public UserVector save(UserVector userVector) {
        return jpaUserVectorRepository.save(userVector);
    }

    @Override
    public Optional<UserVector> findById(Long id) {
        return jpaUserVectorRepository.findById(id);
    }

    @Override
    public List<UserVector> findByUserId(Long userId) {
        return jpaUserVectorRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserVectorRepository.deleteById(id);
    }
}
