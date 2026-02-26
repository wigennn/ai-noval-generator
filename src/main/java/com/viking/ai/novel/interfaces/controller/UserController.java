package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.UserService;
import com.viking.ai.novel.domain.model.User;
import com.viking.ai.novel.interfaces.dto.CreateUserRequest;
import com.viking.ai.novel.interfaces.dto.UserDTO;
import com.viking.ai.novel.interfaces.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper = UserMapper.INSTANCE;
    
    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getPhone(),
                    request.getEmail()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(user));
        } catch (RuntimeException e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据手机号获取用户
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserDTO> getUserByPhone(@PathVariable String phone) {
        return userService.getUserByPhone(phone)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody CreateUserRequest request) {
        try {
            User user = userService.updateUser(
                    id,
                    request.getUsername(),
                    request.getEmail()
            );
            return ResponseEntity.ok(userMapper.toDTO(user));
        } catch (RuntimeException e) {
            log.error("Error updating user", e);
            return ResponseEntity.notFound().build();
        }
    }
}
