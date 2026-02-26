package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.application.service.NovelVectorService;
import com.viking.ai.novel.domain.model.NovelVector;
import com.viking.ai.novel.interfaces.dto.CreateNovelVectorRequest;
import com.viking.ai.novel.interfaces.dto.NovelVectorDTO;
import com.viking.ai.novel.interfaces.mapper.NovelVectorMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说向量关联 API
 */
@RestController
@RequestMapping("/api/novel-vectors")
@RequiredArgsConstructor
@Slf4j
public class NovelVectorController {

    private final NovelVectorService novelVectorService;
    private final NovelVectorMapper novelVectorMapper = NovelVectorMapper.INSTANCE;

    @PostMapping
    public ResponseEntity<NovelVectorDTO> create(@Valid @RequestBody CreateNovelVectorRequest request) {
        try {
            NovelVector entity = novelVectorService.create(
                    request.getNovelId(),
                    request.getVectorId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(novelVectorMapper.toDTO(entity));
        } catch (Exception e) {
            log.error("Error creating novel vector", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NovelVectorDTO> getById(@PathVariable Long id) {
        return novelVectorService.getById(id)
                .map(e -> ResponseEntity.ok(novelVectorMapper.toDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/novel/{novelId}")
    public ResponseEntity<List<NovelVectorDTO>> listByNovelId(@PathVariable Long novelId) {
        List<NovelVectorDTO> list = novelVectorService.listByNovelId(novelId).stream()
                .map(novelVectorMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            novelVectorService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting novel vector", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
