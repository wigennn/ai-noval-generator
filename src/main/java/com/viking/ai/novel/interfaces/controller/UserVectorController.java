package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.CurrentUserService;
import com.viking.ai.novel.application.service.UserVectorService;
import com.viking.ai.novel.domain.model.UserVector;
import com.viking.ai.novel.interfaces.aop.CheckUserVectorOwner;
import com.viking.ai.novel.interfaces.aop.CheckUserId;
import com.viking.ai.novel.interfaces.aop.RequireLogin;
import com.viking.ai.novel.interfaces.dto.CreateUserVectorRequest;
import com.viking.ai.novel.interfaces.dto.UploadUserVectorRequest;
import com.viking.ai.novel.interfaces.dto.UserVectorDTO;
import com.viking.ai.novel.interfaces.mapper.UserVectorMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-vectors")
@RequiredArgsConstructor
@Slf4j
public class UserVectorController {

    private final UserVectorService userVectorService;
    private final CurrentUserService currentUserService;
    private final UserVectorMapper userVectorMapper = UserVectorMapper.INSTANCE;

    @PostMapping
    @RequireLogin
    public ResponseEntity<UserVectorDTO> create(@Valid @RequestBody CreateUserVectorRequest request, HttpSession session) {
        long userId = currentUserService.requireCurrentUserId(session);
        UserVector entity = userVectorService.create(userId, request.getVectorName(), request.getVectorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userVectorMapper.toDTO(entity));
    }

    @PostMapping("/upload")
    @RequireLogin
    public ResponseEntity<UserVectorDTO> upload(@Valid @RequestBody UploadUserVectorRequest request, HttpSession session) {
        long userId = currentUserService.requireCurrentUserId(session);
        UserVector entity = userVectorService.createFromContent(userId, request.getVectorName(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(userVectorMapper.toDTO(entity));
    }

    @GetMapping("/{id}")
    @RequireLogin
    @CheckUserVectorOwner(paramIndex = 0)
    public ResponseEntity<UserVectorDTO> getById(@PathVariable Long id) {
        return userVectorService.getById(id)
                .map(e -> ResponseEntity.ok(userVectorMapper.toDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @RequireLogin
    @CheckUserId(paramIndex = 0)
    public ResponseEntity<List<UserVectorDTO>> listByUserId(@PathVariable Long userId) {
        List<UserVectorDTO> list = userVectorService.listByUserId(userId).stream()
                .map(userVectorMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @CheckUserVectorOwner(paramIndex = 0)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userVectorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
