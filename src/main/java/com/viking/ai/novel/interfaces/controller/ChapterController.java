package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.interfaces.dto.ChapterDTO;
import com.viking.ai.novel.interfaces.dto.CreateChapterRequest;
import com.viking.ai.novel.interfaces.mapper.ChapterMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Slf4j
public class ChapterController {
    
    private final ChapterService chapterService;
    private final ChapterMapper chapterMapper = ChapterMapper.INSTANCE;
    
    /**
     * 生成章节
     */
    @PostMapping
    public ResponseEntity<ChapterDTO> generateChapter(@Valid @RequestBody CreateChapterRequest request) {
        try {
            Chapter chapter = chapterService.generateChapter(
                    request.getNovelId(),
                    request.getChapterNumber(),
                    request.getTitle(),
                    request.getAbstractContent(),
                    request.getAsync() != Boolean.FALSE
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(chapterMapper.toDTO(chapter));
        } catch (Exception e) {
            log.error("Error generating chapter", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 根据ID获取章节
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChapterDTO> getChapter(@PathVariable Long id) {
        return chapterService.getChapterById(id)
                .map(chapter -> ResponseEntity.ok(chapterMapper.toDTO(chapter)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取小说的所有章节
     */
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<List<ChapterDTO>> getChaptersByNovelId(@PathVariable Long novelId) {
        List<ChapterDTO> chapters = chapterService.getChaptersByNovelId(novelId)
                .stream()
                .map(chapterMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chapters);
    }
    
    /**
     * 更新章节
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChapterDTO> updateChapter(
            @PathVariable Long id,
            @RequestBody CreateChapterRequest request) {
        try {
            Chapter chapter = chapterService.updateChapter(
                    id,
                    request.getTitle(),
                    null // content 更新需要单独处理
            );
            return ResponseEntity.ok(chapterMapper.toDTO(chapter));
        } catch (RuntimeException e) {
            log.error("Error updating chapter", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 删除章节
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long id) {
        try {
            chapterService.deleteChapter(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting chapter", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
