package com.viking.ai.novel.interfaces.controller;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import com.viking.ai.novel.interfaces.dto.ChapterStreamPayload;
import com.viking.ai.novel.interfaces.dto.ChapterStreamRequest;
import com.viking.ai.novel.interfaces.dto.StopStreamRequest;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 章节内容 WebSocket 流式生成
 * <p>
 * 客户端：
 * - 连接 /ws
 * - 订阅 /topic/chapters/{novelId}/{chapterNumber}
 * - 发送消息到 /app/chapters/stream 触发生成
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChapterWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final UserModelRepository userModelRepository;
    private final QdrantService qdrantService;
    private final AiModelService aiModelService;

    // 存储正在进行的章节流式生成任务，key: "novelId:chapterNumber"
    private final ConcurrentHashMap<String, AtomicBoolean> activeChapterStreams = new ConcurrentHashMap<>();

    @MessageMapping("/chapters/stream")
    public void streamChapter(ChapterStreamRequest request, Principal principal) {
        if (request.getNovelId() == null || request.getChapterNumber() == null) {
            log.warn("Invalid chapter stream request: {}", request);
            return;
        }
        Long currentUserId = principalToUserId(principal);
        String destination = String.format("/topic/chapters/%d/%d", request.getNovelId(), request.getChapterNumber());
        if (currentUserId == null) {
            messagingTemplate.convertAndSend(destination, new ChapterStreamPayload("error", "未登录"));
            return;
        }

        Long novelId = request.getNovelId();
        Integer chapterNumber = request.getChapterNumber();

        // 创建停止标记
        String streamKey = String.format("%d:%d", novelId, chapterNumber);
        AtomicBoolean stopped = new AtomicBoolean(false);
        activeChapterStreams.put(streamKey, stopped);

        try {
            Novel novel = novelRepository.findById(novelId)
                    .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
            if (!currentUserId.equals(novel.getUserId())) {
                activeChapterStreams.remove(streamKey);
                messagingTemplate.convertAndSend(destination, new ChapterStreamPayload("error", "无权限操作该小说"));
                return;
            }
            UserModel model = userModelRepository.findByUserIdAndType(
                            novel.getUserId(), ModelTypeEnum.NORMAL.getType())
                    .orElseThrow(() -> new RuntimeException("User model not found: " + novel.getUserId()));

            // 查找或创建章节
            Optional<Chapter> existingChapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
            Chapter chapter = existingChapter.orElseGet(() -> {
                Chapter ch = Chapter.builder()
                        .novelId(novelId)
                        .chapterNumber(chapterNumber)
                        .title(request.getTitle())
                        .abstractContent(request.getAbstractContent())
                        .status(1)
                        .build();
                return chapterRepository.save(ch);
            });

            // 构造前文摘要
            List<Chapter> previousChapters = chapterRepository.findByNovelId(novelId)
                    .stream()
                    .filter(c -> c.getChapterNumber() < chapterNumber)
                    .sorted((a, b) -> Integer.compare(a.getChapterNumber(), b.getChapterNumber()))
                    .collect(Collectors.toList());
            List<String> previousAbstracts = previousChapters.stream()
                    .map(Chapter::getAbstractContent)
                    .filter(abstractContent -> abstractContent != null && !abstractContent.isEmpty())
                    .collect(Collectors.toList());

            // 查询向量数据库获取最相关的top3片段
            List<String> relevantSnippets = queryRelevantSnippets(novel, chapter);

            // 标记处理中
            chapter.setStatus(1);
            chapterRepository.save(chapter);

            aiModelService.streamChapterContent(
                    novel.getTitle(),
                    novel.getGenre(),
                    novel.getSettingText(),
                    novel.getStructure(),
                    chapter.getTitle(),
                    chapter.getAbstractContent(),
                    previousAbstracts,
                    novel.getChapterWordCount(),
                    relevantSnippets,
                    model,
                    new AiModelService.ChapterStreamCallback() {
                        @Override
                        public void onDelta(String text) {
                            if (stopped.get()) {
                                return;
                            }
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("delta", text));
                        }

                        @Override
                        public void onComplete(String fullText) {
                            activeChapterStreams.remove(streamKey);
                            if (stopped.get()) {
                                messagingTemplate.convertAndSend(destination,
                                        new ChapterStreamPayload("stopped", null));
                                return;
                            }
                            try {
                                // 生成完成，保存章节、生成摘要、入库向量
                                chapter.setContent(fullText);
                                if (chapter.getAbstractContent() == null || chapter.getAbstractContent().isEmpty()) {
                                    String abstractContent = aiModelService.generateChapterAbstract(fullText, model);
                                    chapter.setAbstractContent(abstractContent);
                                }

                                try {
                                    if (chapter.getVectorId() != null) {
                                        qdrantService.deleteVector(chapter.getVectorId());
                                    }
                                    String vectorId = qdrantService.storeChapter(
                                            chapter.getId().toString(), fullText, model);
                                    chapter.setVectorId(vectorId);
                                } catch (Exception e) {
                                    log.error("Error storing chapter to Qdrant", e);
                                }
                                chapter.setStatus(2);
                                chapterRepository.save(chapter);

                                messagingTemplate.convertAndSend(destination,
                                        new ChapterStreamPayload("complete", null));
                            } catch (Exception e) {
                                log.error("Error finishing streamed chapter {}", chapter.getId(), e);
                                messagingTemplate.convertAndSend(destination,
                                        new ChapterStreamPayload("error", e.getMessage()));
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            activeChapterStreams.remove(streamKey);
                            log.error("Error streaming chapter content", t);
                            messagingTemplate.convertAndSend(destination,
                                    new ChapterStreamPayload("error", t.getMessage()));
                        }
                    }
            );
        } catch (Exception e) {
            activeChapterStreams.remove(streamKey);
            log.error("Error starting chapter stream", e);
            messagingTemplate.convertAndSend(destination,
                    new ChapterStreamPayload("error", e.getMessage()));
        }
    }

    @MessageMapping("/chapters/stop")
    public void stopChapterStream(StopStreamRequest request) {
        if (request.getNovelId() == null || request.getChapterNumber() == null) {
            log.warn("Invalid stop chapter stream request: {}", request);
            return;
        }

        String streamKey = String.format("%d:%d", request.getNovelId(), request.getChapterNumber());
        AtomicBoolean stopped = activeChapterStreams.get(streamKey);
        
        if (stopped != null) {
            stopped.set(true);
            log.info("Stopped chapter stream: {}", streamKey);
        } else {
            log.warn("Chapter stream not found: {}", streamKey);
        }
    }

    private static Long principalToUserId(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 查询相关片段
     */
    private List<String> queryRelevantSnippets(Novel novel, Chapter chapter) {
        List<String> relevantSnippets = new ArrayList<>();
        try {
            // 构建查询文本：章节标题 + 章节摘要 + 世界观设定
            StringBuilder queryBuilder = new StringBuilder();
            if (chapter.getTitle() != null && !chapter.getTitle().isEmpty()) {
                queryBuilder.append("章节标题：").append(chapter.getTitle()).append("\n");
            }
            if (chapter.getAbstractContent() != null && !chapter.getAbstractContent().isEmpty()) {
                queryBuilder.append("章节摘要：").append(chapter.getAbstractContent()).append("\n");
            }
            if (novel.getSettingText() != null && !novel.getSettingText().isEmpty()) {
                queryBuilder.append("世界观设定：").append(novel.getSettingText());
            }

            String queryText = queryBuilder.toString();
            if (!queryText.isEmpty()) {
                // 获取用户的向量模型（用于embedding）
                UserModel vectorModel = userModelRepository.findByUserIdAndType(
                                novel.getUserId(), ModelTypeEnum.VECTOR.getType())
                        .orElse(null);

                if (vectorModel != null) {
                    // 查询向量数据库，获取top3相关片段
                    List<EmbeddingMatch<TextSegment>> matches = qdrantService.searchSimilar(queryText, 3, vectorModel);
                    for (EmbeddingMatch<TextSegment> match : matches) {
                        TextSegment segment = match.embedded();
                        if (segment != null && segment.text() != null) {
                            String text = segment.text().trim();
                            if (!text.isEmpty()) {
                                relevantSnippets.add(text);
                            }
                        }
                    }
                    log.info("Found {} relevant snippets for chapter {} of novel {}",
                            relevantSnippets.size(), chapter.getChapterNumber(), novel.getId());
                } else {
                    log.warn("Vector model not found for user {}, skipping RAG search", novel.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("Error searching relevant snippets from vector database", e);
            // 不中断流程，继续生成章节内容
        }
        return relevantSnippets;
    }
}

