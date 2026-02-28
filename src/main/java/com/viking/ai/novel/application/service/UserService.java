package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.User;
import com.viking.ai.novel.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户（密码会加密存储）
     */
    @Transactional
    public User createUser(String username, String password, String phone, String email) {
        if (email != null && !email.isBlank() && userRepository.findByEmail(email.trim()).isPresent()) {
            throw new RuntimeException("邮箱已被注册: " + email);
        }
        if (phone != null && !phone.isBlank() && userRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("手机号已被注册: " + phone);
        }
        if (username != null && !username.isBlank() && userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已被占用: " + username);
        }

        String encodedPassword = (password != null && !password.isBlank())
                ? passwordEncoder.encode(password)
                : null;

        User user = User.builder()
                .username(username != null && !username.isBlank() ? username : null)
                .password(encodedPassword)
                .phone(phone != null && !phone.isBlank() ? phone : null)
                .email(email != null && !email.isBlank() ? email.trim() : null)
                .score(100)
                .build();

        return userRepository.save(user);
    }

    /**
     * 邮箱 + 密码登录，校验通过返回用户
     */
    public Optional<User> loginByEmail(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim())
                .filter(user -> user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword()))
                .map(user -> user);
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
     * 根据邮箱获取用户
     */
    public Optional<User> getUserByEmail(String email) {
        return email == null || email.isBlank() ? Optional.empty() : userRepository.findByEmail(email.trim());
    }

    /**
     * 根据邮箱获取或创建用户（验证码登录时：不存在则自动注册）
     */
    @Transactional
    public User getOrCreateUserByEmail(String email) {
        String trimmed = email == null ? null : email.trim();
        if (trimmed == null || trimmed.isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        return userRepository.findByEmail(trimmed)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(trimmed)
                            .password(null)
                            .username(null)
                            .phone(null)
                            .score(100)
                            .build();
                    return userRepository.save(user);
                });
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
            if (userRepository.findByEmail(email.trim()).filter(u -> !u.getId().equals(id)).isPresent()) {
                throw new RuntimeException("邮箱已被其他用户使用: " + email);
            }
            user.setEmail(email.trim());
        }

        return userRepository.save(user);
    }
}
