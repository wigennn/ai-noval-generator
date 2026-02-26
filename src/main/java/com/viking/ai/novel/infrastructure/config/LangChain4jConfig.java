package com.viking.ai.novel.infrastructure.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类
 */
@Configuration
public class LangChain4jConfig {
    
    @Value("${langchain4j.embedding-model.open-ai.api-key:}")
    private String openAiApiKey;
    
    @Value("${langchain4j.embedding-model.open-ai.model-name:text-embedding-3-small}")
    private String openAiModelName;
    
    /**
     * 配置 Embedding Model
     * 如果配置了 OpenAI API Key，则使用 OpenAI，否则使用本地模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        // 如果配置了 OpenAI API Key，使用 OpenAI
        if (openAiApiKey != null && !openAiApiKey.isEmpty() && !openAiApiKey.equals("your-api-key")) {
            // 需要添加 langchain4j-open-ai 依赖中的 OpenAiEmbeddingModel
//             return new OpenAiEmbeddingModel(openAiApiKey, openAiModelName);
        }
        // 使用本地模型（需要下载模型文件）
//        return new AllMiniLmL6V2EmbeddingModel();
        return null;
    }
}
