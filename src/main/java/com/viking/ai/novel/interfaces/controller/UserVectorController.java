package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.UserVectorService;
import com.viking.ai.novel.domain.model.UserVector;
import com.viking.ai.novel.interfaces.dto.CreateUserVectorRequest;
import com.viking.ai.novel.interfaces.dto.UploadUserVectorRequest;
import com.viking.ai.novel.interfaces.dto.UserVectorDTO;
import com.viking.ai.novel.interfaces.mapper.UserVectorMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户资料库（用户向量）API
 */
@RestController
@RequestMapping("/api/user-vectors")
@RequiredArgsConstructor
@Slf4j
public class UserVectorController {

    private final UserVectorService userVectorService;
    private final UserVectorMapper userVectorMapper = UserVectorMapper.INSTANCE;

    @PostMapping
    public ResponseEntity<UserVectorDTO> create(@Valid @RequestBody CreateUserVectorRequest request) {
        try {
            UserVector entity = userVectorService.create(
                    request.getUserId(),
                    request.getVectorName(),
                    request.getVectorId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(userVectorMapper.toDTO(entity));
        } catch (Exception e) {
            log.error("Error creating user vector", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 上传资料库：提交文本内容，自动向量化并入库
     */
    @PostMapping("/upload")
    public ResponseEntity<UserVectorDTO> upload(@Valid @RequestBody UploadUserVectorRequest request) {
        try {
            UserVector entity = userVectorService.createFromContent(
                    request.getUserId(),
                    request.getVectorName(),
                    request.getContent()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(userVectorMapper.toDTO(entity));
        } catch (Exception e) {
            log.error("Error uploading user vector", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserVectorDTO> getById(@PathVariable Long id) {
        return userVectorService.getById(id)
                .map(e -> ResponseEntity.ok(userVectorMapper.toDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserVectorDTO>> listByUserId(@PathVariable Long userId) {
        List<UserVectorDTO> list = userVectorService.listByUserId(userId).stream()
                .map(userVectorMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            userVectorService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting user vector", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
