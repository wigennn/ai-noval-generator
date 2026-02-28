package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.NovelVectorService;
import com.viking.ai.novel.interfaces.aop.CheckNovelIdOwner;
import com.viking.ai.novel.interfaces.aop.CheckNovelVectorOwner;
import com.viking.ai.novel.interfaces.aop.RequireLogin;
import com.viking.ai.novel.interfaces.dto.NovelVectorDTO;
import com.viking.ai.novel.interfaces.mapper.NovelVectorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/novel-vectors")
@RequiredArgsConstructor
@Slf4j
public class NovelVectorController {

    private final NovelVectorService novelVectorService;
    private final NovelVectorMapper novelVectorMapper = NovelVectorMapper.INSTANCE;

    @GetMapping("/{id}")
    @RequireLogin
    @CheckNovelVectorOwner(paramIndex = 0)
    public ResponseEntity<NovelVectorDTO> getById(@PathVariable Long id) {
        return novelVectorService.getById(id)
                .map(e -> ResponseEntity.ok(novelVectorMapper.toDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/novel/{novelId}")
    @RequireLogin
    @CheckNovelIdOwner(paramIndex = 0)
    public ResponseEntity<List<NovelVectorDTO>> listByNovelId(@PathVariable Long novelId) {
        List<NovelVectorDTO> list = novelVectorService.listByNovelId(novelId).stream()
                .map(novelVectorMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @CheckNovelVectorOwner(paramIndex = 0)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        novelVectorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
