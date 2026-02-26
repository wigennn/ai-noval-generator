package com.viking.ai.novel.infrastructure.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
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

    @Value("${langchain4j.embedding-model.open-ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;
    
    /**
     * 配置 Embedding Model
     * 如果配置了 OpenAI API Key，则使用 OpenAI，否则使用本地模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        // 如果配置了 OpenAI API Key，使用 OpenAI
        if (openAiApiKey != null && !openAiApiKey.isEmpty()) {
            // 需要添加 langchain4j-open-ai 依赖中的 OpenAiEmbeddingModel
             return OpenAiEmbeddingModel.builder().apiKey(openAiApiKey).modelName(openAiModelName).build();
        }
        return null;
    }
}
