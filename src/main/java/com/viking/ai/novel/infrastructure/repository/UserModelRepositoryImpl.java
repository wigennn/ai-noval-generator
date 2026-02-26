package com.viking.ai.novel.infrastructure.repository;

import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserModelRepositoryImpl implements UserModelRepository {
    private final JpaUserModelRepository jpaUserModelRepository;

    @Override
    public UserModel save(UserModel userModel) {
        return jpaUserModelRepository.save(userModel);
    }

    @Override
    public Optional<UserModel> findById(Long id) {
        return jpaUserModelRepository.findById(id);
    }

    @Override
    public List<UserModel> findByUserId(Long userId) {
        return jpaUserModelRepository.findByUserId(userId);
    }
}
