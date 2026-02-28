package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.application.service.CurrentUserService;
import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.interfaces.aop.CheckChapterOwner;
import com.viking.ai.novel.interfaces.aop.CheckNovelIdOwner;
import com.viking.ai.novel.interfaces.aop.CheckRequestNovelId;
import com.viking.ai.novel.interfaces.aop.RequireLogin;
import com.viking.ai.novel.interfaces.dto.ChapterDTO;
import com.viking.ai.novel.interfaces.dto.CreateChapterRequest;
import com.viking.ai.novel.interfaces.mapper.ChapterMapper;
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
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Slf4j
public class ChapterController {

    private final ChapterService chapterService;
    private final CurrentUserService currentUserService;
    private final ChapterMapper chapterMapper = ChapterMapper.INSTANCE;

    @PostMapping
    @RequireLogin
    @CheckRequestNovelId(paramIndex = 0, field = "novelId")
    public ResponseEntity<ChapterDTO> generateChapter(@Valid @RequestBody CreateChapterRequest request, HttpSession session) {
        Chapter chapter = chapterService.generateChapter(
                request.getNovelId(),
                request.getChapterNumber(),
                request.getTitle(),
                request.getAbstractContent(),
                request.getAsync() != Boolean.FALSE
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterMapper.toDTO(chapter));
    }

    @GetMapping("/{id}")
    @RequireLogin
    @CheckChapterOwner(paramIndex = 0)
    public ResponseEntity<ChapterDTO> getChapter(@PathVariable Long id) {
        return chapterService.getChapterById(id)
                .map(chapter -> ResponseEntity.ok(chapterMapper.toDTO(chapter)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/novel/{novelId}")
    @RequireLogin
    @CheckNovelIdOwner(paramIndex = 0)
    public ResponseEntity<List<ChapterDTO>> getChaptersByNovelId(@PathVariable Long novelId) {
        List<ChapterDTO> chapters = chapterService.getChaptersByNovelId(novelId)
                .stream()
                .map(chapterMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chapters);
    }

    @PutMapping("/{id}")
    @RequireLogin
    @CheckChapterOwner(paramIndex = 0)
    public ResponseEntity<ChapterDTO> updateChapter(
            @PathVariable Long id,
            @RequestBody CreateChapterRequest request) {
        Chapter chapter = chapterService.updateChapter(id, request.getTitle(), null);
        return ResponseEntity.ok(chapterMapper.toDTO(chapter));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @CheckChapterOwner(paramIndex = 0)
    public ResponseEntity<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.noContent().build();
    }
}
