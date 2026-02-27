package com.viking.ai.novel.infrastructure.ai;

import com.viking.ai.novel.domain.model.UserModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
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
    
    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private double temperature;

    public ChatLanguageModel getChatModel(UserModel model) {
        return OpenAiChatModel.builder()
                .apiKey(model.getApiKey())
                .baseUrl(model.getModelUrl())
                .modelName(model.getModelName())
                .temperature(temperature)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    /**
     * 获取支持流式输出的 ChatLanguageModel
     */
    public StreamingChatLanguageModel getStreamingChatModel(UserModel model) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(model.getApiKey())
                .baseUrl(model.getModelUrl())
                .modelName(model.getModelName())
                .temperature(temperature)
                .timeout(Duration.ofSeconds(6000))
                .maxTokens(8192)
                .build();
    }

    public EmbeddingModel getEmbeddingModel(UserModel model) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
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
     * 流式生成回调接口
     */
    public interface StreamCallback {
        void onDelta(String text);
        void onComplete(String fullText);
        void onError(Throwable t);
    }

    /**
     * 章节内容流式生成回调（保持向后兼容）
     */
    public interface ChapterStreamCallback extends StreamCallback {
    }

    /**
     * 生成章节内容（流式）
     */
    public void streamChapterContent(String novelTitle, String genre, String settingText,
                                     String structure, String chapterTitle, String chapterAbstract,
                                     List<String> previousChapters, Integer chapterWordCount,
                                     UserModel model, ChapterStreamCallback callback) {
        StreamingChatLanguageModel streamingModel = getStreamingChatModel(model);

        StringBuilder contextBuilder = new StringBuilder();
        if (previousChapters != null && !previousChapters.isEmpty()) {
            contextBuilder.append("前文摘要：\n");
            for (int i = 0; i < previousChapters.size(); i++) {
                contextBuilder.append(String.format("第%d章：%s\n", i + 1, previousChapters.get(i)));
            }
        }

        String wordCountRequirement;
        if (chapterWordCount != null && chapterWordCount > 0) {
            int minWords = (int) (chapterWordCount * 0.8);
            int maxWords = (int) (chapterWordCount * 1.2);
            wordCountRequirement = String.format("字数控制在%d-%d字（目标字数：%d字）", minWords, maxWords, chapterWordCount);
        } else {
            wordCountRequirement = "字数控制在3000-5000字";
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
            4. %s
            5. 章节结尾要有适当的悬念或转折
            """, novelTitle, genre, settingText, structure, contextBuilder.toString(),
                chapterTitle, chapterAbstract, genre, wordCountRequirement);

        StringBuilder full = new StringBuilder();

        streamingModel.generate(prompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                full.append(token);
                if (callback != null) {
                    callback.onDelta(token);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Error streaming chapter content", error);
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                if (callback != null) {
                    callback.onComplete(full.toString());
                }
            }
        });
    }
    
    /**
     * 生成小说结构（流式）
     */
    public void streamNovelStructure(String title, String genre, String settingText,
                                     Integer chapterNumber, UserModel model, StreamCallback callback) {
        StreamingChatLanguageModel streamingModel = getStreamingChatModel(model);

        // 根据总章数生成章节规划要求
        String chapterPlanningRequirement;
        if (chapterNumber != null && chapterNumber > 0) {
            chapterPlanningRequirement = String.format("规划%d章", chapterNumber);
        } else {
            chapterPlanningRequirement = "至少规划15-20章";
        }

        String prompt = String.format("""
            你是一位专业的小说创作顾问。请根据以下信息生成一部小说的详细架构：
            
            【基本信息】
            标题：%s
            题材：%s
            世界观设定：%s
            
            【要求】
            请生成一份完整的小说架构，包含以下部分：
            
            1. 【核心种子】
                - 故事的核心冲突或主题
                - 故事的独特卖点
            
            2. 【主要角色】
                - 主角：姓名、性格特点、背景、目标与动机
                - 重要配角：姓名、性格特点、与主角的关系
                - 反派（如有）：姓名、性格特点、动机与目标
            
            3. 【世界观设定】
                - 时代背景
                - 地理环境
                - 社会结构
                - 特殊规则或设定（如魔法、科技等）
            
            4. 【故事结构】
                - 开端：故事如何开始，主角的初始状态
                - 发展：主要冲突的展开，角色关系的发展
                - 高潮：故事的关键转折点或最大冲突
                - 结局：故事的收尾方式
            
            5. 【主要情节线】
                - 主线：故事的核心情节发展
                - 支线：次要情节线（至少2-3条）
                - 伏笔：需要埋下的重要线索
            
            6. 【章节规划】
                - %s
                - 每章包含：章节序号、章节标题、章节核心事件、章节作用（推进主线/支线/人物塑造等）
            
            【输出格式】
            请使用清晰的分段和标题，确保结构清晰、内容详实，便于后续章节创作。
            """, title, genre, settingText, chapterPlanningRequirement);

        StringBuilder full = new StringBuilder();

        streamingModel.generate(prompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                full.append(token);
                if (callback != null) {
                    callback.onDelta(token);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Error streaming novel structure", error);
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                if (callback != null) {
                    callback.onComplete(full.toString());
                }
            }
        });
    }

    /**
     * 生成章节大纲（流式）
     */
    public void streamChapterOutline(String title, String genre, String settingText,
                                     String structure, UserModel model, StreamCallback callback) {
        StreamingChatLanguageModel streamingModel = getStreamingChatModel(model);

        String prompt = String.format("""
            你是一位专业的小说创作顾问。请根据以下信息生成详细的章节大纲：
            
            【基本信息】
            小说标题：%s
            题材：%s
            世界观设定：%s
            
            【小说架构】
            %s
            
            【要求】
            请生成一份完整的章节大纲，要求：
            
            1. 根据小说架构中的章节规划，为每一章生成详细大纲
            2. 每章大纲应包含：
               - 章节序号和标题
               - 章节核心事件（主要发生什么）
               - 出场角色
               - 章节目标（推进主线/支线/人物塑造/世界观展示等）
               - 章节结尾的悬念或转折点
               - 与前后章的衔接点
            
            3. 确保章节之间的逻辑连贯性
            4. 合理分配情节节奏（紧张/舒缓交替）
            5. 确保重要伏笔和线索的埋设时机
            
            【输出格式】
            请按照以下格式输出：
            
            ## 第X章 [章节标题]
            - **核心事件**：[描述]
            - **出场角色**：[角色列表]
            - **章节目标**：[说明]
            - **章节结尾**：[悬念或转折]
            - **衔接点**：[与前后章的联系]
            
            【注意事项】
            - 章节数量应与架构中的规划一致
            - 每章大纲应详细但不过于冗长
            - 确保整体故事节奏的合理性
            """, title, genre, settingText, structure != null ? structure : "无");

        StringBuilder full = new StringBuilder();

        streamingModel.generate(prompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                full.append(token);
                if (callback != null) {
                    callback.onDelta(token);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Error streaming chapter outline", error);
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                if (callback != null) {
                    callback.onComplete(response.content().text());
                }
            }
        });
    }

    /**
     * 生成章节摘要
     */
    public String generateChapterAbstract(String chapterContent, UserModel model) {
        ChatLanguageModel chatModel = getChatModel(model);
        
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
