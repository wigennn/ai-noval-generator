package com.viking.ai.novel.infrastructure.ai;

import com.viking.ai.novel.domain.model.UserModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class QdrantService {
    
    @Value("${qdrant.host:localhost}")
    private String qdrantHost;
    
    @Value("${qdrant.port:6333}")
    private int qdrantPort;

    @Value("${qdrant.port:6334}")
    private int qdrantGrpcPort;

    // 使用 ConcurrentHashMap 来存储不同集合的 EmbeddingStore 实例
    private final Map<String, EmbeddingStore<TextSegment>> embeddingStoreMap = new ConcurrentHashMap<>();

    private static final Integer size = 1536;

    @Autowired
    private AiModelService aiModelService;

    // Qdrant gRPC 客户端，用于集合管理
    private QdrantClient qdrantClient;

    @PostConstruct
    public void init() {
        try {
            // 初始化 Qdrant gRPC 客户端
            QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(qdrantHost, qdrantGrpcPort, false).build();
            qdrantClient = new QdrantClient(grpcClient);
            log.info("Qdrant client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Qdrant client", e);
            throw new RuntimeException("Failed to initialize Qdrant client", e);
        }
    }

    /**
     * 确保集合存在，如果不存在则创建
     */
    private void ensureCollectionExists(String collectionName) {
        try {
            // 检查集合是否存在
            List<String> collections = qdrantClient.listCollectionsAsync().get();

            if (!collections.contains(collectionName)) {
                log.info("Collection {} does not exist, creating...", collectionName);

                // 创建集合配置
                Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                        .setSize(size) // 默认向量维度，可以根据实际模型调整
                        .setDistance(Collections.Distance.Cosine)
                        .build();

                Collections.CreateCollection.Builder createCollectionBuilder = Collections.CreateCollection.newBuilder()
                        .setCollectionName(collectionName)
                        .setVectorsConfig(Collections.VectorsConfig.newBuilder()
                                .setParams(vectorParams)
                                .build());

                // 执行创建操作
                qdrantClient.createCollectionAsync(createCollectionBuilder.build()).get();
                log.info("Collection {} created successfully", collectionName);
            } else {
                log.debug("Collection {} already exists", collectionName);
            }
        } catch (Exception e) {
            log.error("Error ensuring collection {} exists", collectionName, e);
            throw new RuntimeException("Failed to ensure collection exists: " + collectionName, e);
        }
    }
    
    private EmbeddingStore<TextSegment> getEmbeddingStore(String collectionName) {
        // 先确保集合存在
        ensureCollectionExists(collectionName);

        // 使用 computeIfAbsent 确保线程安全的懒加载
        return embeddingStoreMap.computeIfAbsent(collectionName, name -> {
            log.info("Creating EmbeddingStore for collection: {}", name);
            return QdrantEmbeddingStore.builder()
                    .host(qdrantHost)
                    .port(qdrantGrpcPort) // REST API 端口
                    .collectionName(name)
                    .useTls(false)
                    .build();
        });
    }
    
    /**
     * 存储文本到向量数据库，返回向量ID
     */
    public String storeText(String content, UserModel model, String collectionName) {
        EmbeddingModel embeddingModel = aiModelService.getEmbeddingModel(model);

        TextSegment segment = TextSegment.from(content);
        Embedding embedding = embeddingModel.embed(segment).content();
        String id = getEmbeddingStore(collectionName).add(embedding);
        log.info("Stored text to Qdrant with vector ID: {}", id);
        return id;
    }
    
    /**
     * 存储章节内容到向量数据库
     */
    public String storeChapter(String chapterId, String content, UserModel model, String collectionName) {
        try {
            EmbeddingModel embeddingModel = aiModelService.getEmbeddingModel(model);
            TextSegment segment = TextSegment.from(content);
            Embedding embedding = embeddingModel.embed(segment).content();
            String id = getEmbeddingStore(collectionName).add(embedding);
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
    public List<EmbeddingMatch<TextSegment>> searchSimilar(String query, int maxResults, UserModel model, String collectionName) {
        try {
            EmbeddingModel embeddingModel = aiModelService.getEmbeddingModel(model);
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            return getEmbeddingStore(collectionName).findRelevant(queryEmbedding, maxResults, 0.0);
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

    /**
     * 清理资源
     */
    public void cleanup() {
        if (qdrantClient != null) {
            try {
                qdrantClient.close();
                log.info("Qdrant client closed");
            } catch (Exception e) {
                log.error("Error closing Qdrant client", e);
            }
        }
    }
}
