package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 章节内容生成：核心逻辑供同步调用或 MQ 消费者调用
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterGenerationTaskService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final AiModelService aiModelService;
    private final QdrantService qdrantService;
    private final UserModelRepository userModelRepository;

    /**
     * 执行章节内容生成（同步逻辑，供实时调用或 MQ 消费者调用）
     */
    @Transactional
    public void doGenerateChapterContent(Long novelId, Long chapterId, Long taskId) {

    }
}
