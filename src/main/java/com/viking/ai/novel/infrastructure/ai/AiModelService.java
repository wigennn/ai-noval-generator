package com.viking.ai.novel.infrastructure.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * AI 模型服务，用于调用大模型生成内容
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiModelService {
    
    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;
    
    @Value("${langchain4j.open-ai.chat-model.base-url:https://api.openai.com/v1}")
    private String baseUrl;
    
    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4}")
    private String modelName;
    
    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private double temperature;
    
    /**
     * 获取默认的 ChatLanguageModel
     */
    public ChatLanguageModel getDefaultChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
    
    /**
     * 根据用户配置创建 ChatLanguageModel
     */
    public ChatLanguageModel createChatModel(String apiKey, String baseUrl, String modelName) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
    
    /**
     * 生成小说结构
     */
    public String generateNovelStructure(String title, String genre, String settingText) {
        ChatLanguageModel chatModel = getDefaultChatModel();
        
        String prompt = String.format("""
            请根据以下信息生成一部小说的详细结构：
            
            标题：%s
            题材：%s
            世界观设定：%s
            
            请生成包含以下内容的小说结构：
            1. 主要角色介绍
            2. 故事背景
            3. 主要情节线
            4. 章节大纲（至少10章，每章包含标题和简要内容）
            
            请以结构化的格式输出，便于后续章节生成。
            """, title, genre, settingText);
        
        try {
            String response = chatModel.generate(prompt);
            log.info("Generated novel structure for title: {}", title);
            return response;
        } catch (Exception e) {
            log.error("Error generating novel structure", e);
            throw new RuntimeException("Failed to generate novel structure", e);
        }
    }
    
    /**
     * 生成章节内容
     */
    public String generateChapterContent(String novelTitle, String genre, String settingText, 
                                         String structure, String chapterTitle, String chapterAbstract,
                                         List<String> previousChapters) {
        ChatLanguageModel chatModel = getDefaultChatModel();
        
        StringBuilder contextBuilder = new StringBuilder();
        if (previousChapters != null && !previousChapters.isEmpty()) {
            contextBuilder.append("前文摘要：\n");
            for (int i = 0; i < previousChapters.size(); i++) {
                contextBuilder.append(String.format("第%d章：%s\n", i + 1, previousChapters.get(i)));
            }
        }
        
        String prompt = String.format("""
            请根据以下信息生成小说章节内容：
            
            小说标题：%s
            题材：%s
            世界观设定：%s
            小说结构：%s
            
            %s
            
            章节标题：%s
            章节摘要：%s
            
            请生成完整的章节内容，要求：
            1. 内容符合世界观设定
            2. 情节连贯，与前文衔接自然
            3. 文笔流畅，符合%s题材的风格
            4. 字数控制在3000-5000字
            5. 章节结尾要有适当的悬念或转折
            """, novelTitle, genre, settingText, structure, contextBuilder.toString(), 
            chapterTitle, chapterAbstract, genre);
        
        try {
            String response = chatModel.generate(prompt);
            log.info("Generated chapter content for: {}", chapterTitle);
            return response;
        } catch (Exception e) {
            log.error("Error generating chapter content", e);
            throw new RuntimeException("Failed to generate chapter content", e);
        }
    }
    
    /**
     * 生成章节摘要
     */
    public String generateChapterAbstract(String chapterContent) {
        ChatLanguageModel chatModel = getDefaultChatModel();
        
        String prompt = String.format("""
            请为以下章节内容生成一个简洁的摘要（不超过200字）：
            
            %s
            """, chapterContent);
        
        try {
            String response = chatModel.generate(prompt);
            log.info("Generated chapter abstract");
            return response;
        } catch (Exception e) {
            log.error("Error generating chapter abstract", e);
            throw new RuntimeException("Failed to generate chapter abstract", e);
        }
    }
}
