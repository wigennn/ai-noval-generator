package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.NovelExportService;
import com.viking.ai.novel.application.service.NovelService;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.interfaces.dto.CreateNovelRequest;
import com.viking.ai.novel.interfaces.dto.NovelDTO;
import com.viking.ai.novel.interfaces.mapper.NovelMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
@Slf4j
public class NovelController {
    
    private final NovelService novelService;
    private final NovelExportService novelExportService;
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
                    request.getSettingText(),
                    request.getAsync() != Boolean.FALSE
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
     * 导出小说章节（TXT / Markdown / Word）
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<org.springframework.core.io.Resource> export(
            @PathVariable Long id,
            @RequestParam(defaultValue = "txt") String format) {
        try {
            NovelExportService.ExportFormat fmt = NovelExportService.ExportFormat.from(format);
            NovelExportService.ExportResult result = novelExportService.export(id, fmt);
            String encodedFilename = URLEncoder.encode(result.filename(), StandardCharsets.UTF_8).replace("+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(result.contentType()));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
            return ResponseEntity.ok().headers(headers).body(result.resource());
        } catch (RuntimeException e) {
            log.error("Export failed for novel {}", id, e);
            return ResponseEntity.notFound().build();
        }
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
    
    /**
     * 重新生成小说架构
     */
    @PostMapping("/{id}/regenerate-structure")
    public ResponseEntity<NovelDTO> regenerateStructure(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean async) {
        try {
            Novel novel = novelService.regenerateNovelStructure(id, async);
            return ResponseEntity.ok(novelMapper.toDTO(novel));
        } catch (RuntimeException e) {
            log.error("Error regenerating novel structure", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error regenerating novel structure", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 生成章节大纲
     */
    @PostMapping("/{id}/generate-outline")
    public ResponseEntity<NovelDTO> generateChapterOutline(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean async) {
        try {
            Novel novel = novelService.generateChapterOutline(id, async);
            return ResponseEntity.ok(novelMapper.toDTO(novel));
        } catch (RuntimeException e) {
            log.error("Error generating chapter outline", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error generating chapter outline", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
