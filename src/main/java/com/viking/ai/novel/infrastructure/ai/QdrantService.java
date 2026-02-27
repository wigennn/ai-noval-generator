package com.viking.ai.novel.infrastructure.ai;

import com.viking.ai.novel.domain.model.UserModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QdrantService {
    
    @Value("${qdrant.host:localhost}")
    private String qdrantHost;
    
    @Value("${qdrant.port:6333}")
    private int qdrantPort;
    
    @Value("${qdrant.collection-name:novel-chapters}")
    private String collectionName;
    private EmbeddingStore<TextSegment> embeddingStore;
    private AiModelService aiModelService;
    
    private EmbeddingStore<TextSegment> getEmbeddingStore() {
        if (embeddingStore == null) {
            embeddingStore = QdrantEmbeddingStore.builder()
                    .host(qdrantHost)
                    .port(qdrantPort)
                    .collectionName(collectionName)
                    .build();
        }
        return embeddingStore;
    }
    
    /**
     * 存储文本到向量数据库，返回向量ID
     */
    public String storeText(String content, UserModel model) {
        EmbeddingModel embeddingModel = aiModelService.getEmbeddingModel(model);

        TextSegment segment = TextSegment.from(content);
        Embedding embedding = embeddingModel.embed(segment).content();
        String id = getEmbeddingStore().add(embedding);
        log.info("Stored text to Qdrant with vector ID: {}", id);
        return id;
    }
    
    /**
     * 存储章节内容到向量数据库
     */
    public String storeChapter(String chapterId, String content, UserModel model) {
        try {
            EmbeddingModel embeddingModel = aiModelService.getEmbeddingModel(model);
            TextSegment segment = TextSegment.from(content);
            Embedding embedding = embeddingModel.embed(segment).content();
            String id = getEmbeddingStore().add(embedding);
            log.info("Stored chapter {} to Qdrant with vector ID: {}", chapterId, id);
            return id;
        } catch (Exception e) {
            log.error("Error storing chapter to Qdrant", e);
            throw new RuntimeException("Failed to store chapter to Qdrant", e);
        }
    }
    
    /**
     * 根据查询文本搜索相似章节
     */
    public List<EmbeddingMatch<TextSegment>> searchSimilar(String query, int maxResults, UserModel model) {
        try {
            EmbeddingModel embeddingModel = aiModelService.getEmbeddingModel(model);
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            return getEmbeddingStore().findRelevant(queryEmbedding, maxResults, 0.0);
        } catch (Exception e) {
            log.error("Error searching in Qdrant", e);
            throw new RuntimeException("Failed to search in Qdrant", e);
        }
    }
    
    /**
     * 删除向量
     */
    public void deleteVector(String vectorId) {
        try {
//            getEmbeddingStore().remove(vectorId);
            log.info("Deleted vector {} from Qdrant", vectorId);
        } catch (Exception e) {
            log.error("Error deleting vector from Qdrant", e);
            throw new RuntimeException("Failed to delete vector from Qdrant", e);
        }
    }
}
