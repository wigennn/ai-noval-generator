package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.CurrentUserService;
import com.viking.ai.novel.application.service.NovelExportService;
import com.viking.ai.novel.application.service.NovelService;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.interfaces.aop.CheckNovelOwner;
import com.viking.ai.novel.interfaces.aop.CheckUserId;
import com.viking.ai.novel.interfaces.aop.RequireLogin;
import com.viking.ai.novel.interfaces.dto.CreateNovelRequest;
import com.viking.ai.novel.interfaces.dto.NovelDTO;
import com.viking.ai.novel.interfaces.mapper.NovelMapper;
import jakarta.servlet.http.HttpSession;
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
    private final CurrentUserService currentUserService;
    private final NovelMapper novelMapper = NovelMapper.INSTANCE;

    @PostMapping
    @RequireLogin
    public ResponseEntity<NovelDTO> createNovel(@Valid @RequestBody CreateNovelRequest request, HttpSession session) {
        long userId = currentUserService.requireCurrentUserId(session);
        Novel novel = novelService.createNovel(
                userId,
                request.getTitle(),
                request.getGenre(),
                request.getSettingText(),
                request.getChapterNumber(),
                request.getChapterWordCount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(novelMapper.toDTO(novel));
    }

    @GetMapping("/{id}")
    @RequireLogin
    @CheckNovelOwner(paramIndex = 0)
    public ResponseEntity<NovelDTO> getNovel(@PathVariable Long id) {
        return novelService.getNovelById(id)
                .map(novel -> ResponseEntity.ok(novelMapper.toDTO(novel)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/export")
    @RequireLogin
    @CheckNovelOwner(paramIndex = 0)
    public ResponseEntity<org.springframework.core.io.Resource> export(
            @PathVariable Long id,
            @RequestParam(defaultValue = "txt") String format) {
        NovelExportService.ExportFormat fmt = NovelExportService.ExportFormat.from(format);
        NovelExportService.ExportResult result = novelExportService.export(id, fmt);
        String encodedFilename = URLEncoder.encode(result.filename(), StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.contentType()));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
        return ResponseEntity.ok().headers(headers).body(result.resource());
    }

    @GetMapping("/user/{userId}")
    @RequireLogin
    @CheckUserId(paramIndex = 0)
    public ResponseEntity<List<NovelDTO>> getNovelsByUserId(@PathVariable Long userId) {
        List<NovelDTO> novels = novelService.getNovelsByUserId(userId)
                .stream()
                .map(novelMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(novels);
    }

    @PutMapping("/{id}")
    @RequireLogin
    @CheckNovelOwner(paramIndex = 0)
    public ResponseEntity<NovelDTO> updateNovel(
            @PathVariable Long id,
            @RequestBody CreateNovelRequest request) {
        Novel novel = novelService.updateNovel(
                id,
                request.getTitle(),
                request.getGenre(),
                request.getSettingText(),
                request.getChapterNumber(),
                request.getChapterWordCount()
        );
        return ResponseEntity.ok(novelMapper.toDTO(novel));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @CheckNovelOwner(paramIndex = 0)
    public ResponseEntity<Void> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/regenerate-structure")
    @RequireLogin
    @CheckNovelOwner(paramIndex = 0)
    public ResponseEntity<NovelDTO> regenerateStructure(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean async) {
        Novel novel = novelService.regenerateNovelStructure(id, async);
        return ResponseEntity.ok(novelMapper.toDTO(novel));
    }

    @PostMapping("/{id}/generate-outline")
    @RequireLogin
    @CheckNovelOwner(paramIndex = 0)
    public ResponseEntity<NovelDTO> generateChapterOutline(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean async) {
        Novel novel = novelService.generateChapterOutline(id, async);
        return ResponseEntity.ok(novelMapper.toDTO(novel));
    }
}
