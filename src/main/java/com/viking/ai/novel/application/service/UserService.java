package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.User;
import com.viking.ai.novel.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 创建用户
     */
    @Transactional
    public User createUser(String username, String password, String phone, String email) {
        // 检查手机号是否已存在
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("Phone number already exists: " + phone);
        }
        
        // 检查用户名是否已存在
        if (username != null && userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }
        
        User user = User.builder()
                .username(username)
                .password(password) // 实际应用中应该加密
                .phone(phone)
                .email(email)
                .build();
        
        return userRepository.save(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 根据手机号获取用户
     */
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(Long id, String username, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        if (username != null) {
            user.setUsername(username);
        }
        if (email != null) {
            user.setEmail(email);
        }
        
        return userRepository.save(user);
    }
}
