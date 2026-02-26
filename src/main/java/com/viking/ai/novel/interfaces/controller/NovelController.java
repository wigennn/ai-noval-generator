package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.NovelService;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.interfaces.dto.CreateNovelRequest;
import com.viking.ai.novel.interfaces.dto.NovelDTO;
import com.viking.ai.novel.interfaces.mapper.NovelMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
@Slf4j
public class NovelController {
    
    private final NovelService novelService;
    private final NovelMapper novelMapper = NovelMapper.INSTANCE;
    
    /**
     * 创建小说
     */
    @PostMapping
    public ResponseEntity<NovelDTO> createNovel(@Valid @RequestBody CreateNovelRequest request) {
        try {
            Novel novel = novelService.createNovel(
                    request.getUserId(),
                    request.getTitle(),
                    request.getGenre(),
                    request.getSettingText()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(novelMapper.toDTO(novel));
        } catch (Exception e) {
            log.error("Error creating novel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 根据ID获取小说
     */
    @GetMapping("/{id}")
    public ResponseEntity<NovelDTO> getNovel(@PathVariable Long id) {
        return novelService.getNovelById(id)
                .map(novel -> ResponseEntity.ok(novelMapper.toDTO(novel)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取用户的所有小说
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NovelDTO>> getNovelsByUserId(@PathVariable Long userId) {
        List<NovelDTO> novels = novelService.getNovelsByUserId(userId)
                .stream()
                .map(novelMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(novels);
    }
    
    /**
     * 更新小说
     */
    @PutMapping("/{id}")
    public ResponseEntity<NovelDTO> updateNovel(
            @PathVariable Long id,
            @RequestBody CreateNovelRequest request) {
        try {
            Novel novel = novelService.updateNovel(
                    id,
                    request.getTitle(),
                    request.getGenre(),
                    request.getSettingText()
            );
            return ResponseEntity.ok(novelMapper.toDTO(novel));
        } catch (RuntimeException e) {
            log.error("Error updating novel", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 删除小说
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNovel(@PathVariable Long id) {
        try {
            novelService.deleteNovel(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting novel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
