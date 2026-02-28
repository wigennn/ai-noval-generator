package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.CurrentUserService;
import com.viking.ai.novel.application.service.EmailVerificationService;
import com.viking.ai.novel.application.service.UserService;
import com.viking.ai.novel.domain.model.User;
import com.viking.ai.novel.interfaces.dto.LoginByCodeRequest;
import com.viking.ai.novel.interfaces.dto.LoginRequest;
import com.viking.ai.novel.interfaces.dto.RegisterRequest;
import com.viking.ai.novel.interfaces.dto.SendCodeRequest;
import com.viking.ai.novel.interfaces.dto.UserDTO;
import com.viking.ai.novel.interfaces.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final UserMapper userMapper = UserMapper.INSTANCE;

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            emailVerificationService.sendCode(request.getEmail());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Send code failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /**
     * 验证码登录（验证通过则登录；该邮箱未注册则自动创建账号并登录）
     */
    @PostMapping("/login-by-code")
    public ResponseEntity<UserDTO> loginByCode(@Valid @RequestBody LoginByCodeRequest request, HttpSession session) {
        if (!emailVerificationService.verifyAndConsume(request.getEmail(), request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getOrCreateUserByEmail(request.getEmail());
        session.setAttribute(CurrentUserService.SESSION_USER_ID, user.getId());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    /**
     * 邮箱 + 密码登录
     */
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return userService.loginByEmail(request.getEmail(), request.getPassword())
                .map(user -> {
                    session.setAttribute(CurrentUserService.SESSION_USER_ID, user.getId());
                    return ResponseEntity.ok(userMapper.toDTO(user));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * 邮箱注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getPhone(),
                    request.getEmail()
            );
            session.setAttribute(CurrentUserService.SESSION_USER_ID, user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(user));
        } catch (RuntimeException e) {
            log.warn("Register failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(HttpSession session) {
        Long userId = (Long) session.getAttribute(CurrentUserService.SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
