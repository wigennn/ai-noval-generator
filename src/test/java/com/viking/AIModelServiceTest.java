package com.viking;

import com.viking.ai.novel.NovelGeneratorApplication;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NovelGeneratorApplication.class)
public class AIModelServiceTest {

    @Autowired
    private AiModelService aiModelService;
    
    @Autowired
    private UserModelRepository userModelRepository;

    @Test
    public void testGenerateNovelStructure() {
        String title = "《星辰变》";
        String genre = "科幻";
        String settingText = "一个位于遥远的太空中的故事，涉及一群星际角色必须克服各种障碍才能到达目的地。";
        Integer chapterNumber = 15;

        // 查找测试模型
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("测试模型未找到"));

        String structure = aiModelService.generateNovelStructure(title, genre, settingText, chapterNumber, model);
        
        assertNotNull(structure, "生成的小说结构不应为null");
        assertFalse(structure.isEmpty(), "生成的小说结构不应为空");
        System.out.println("=== 生成的小说结构 ===");
        System.out.println(structure);
    }

    @Test
    public void testGenerateChapterOutline() {
        String title = "《银河探险》";
        String genre = "科幻冒险";
        String settingText = "23世纪的人类已经能够进行星际旅行，但在遥远的星系中仍有许多未知的危险。";
        String structure = """
            【核心种子】
            故事围绕一支探险队寻找传说中的失落文明展开
            
            【章节规划】
            规划10章
            """;

        // 查找测试模型
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("测试模型未找到"));

        String outline = aiModelService.generateChapterOutline(title, genre, settingText, structure, model);
        
        assertNotNull(outline, "生成的章节大纲不应为null");
        assertFalse(outline.isEmpty(), "生成的章节大纲不应为空");
        System.out.println("=== 生成的章节大纲 ===");
        System.out.println(outline);
    }

    @Test
    public void testGenerateChapterContent() {
        String novelTitle = "《时空旅者》";
        String genre = "科幻悬疑";
        String settingText = "在一个可以穿越时空的世界里，主角发现了改变历史的秘密。";
        String structure = "主线：主角发现时空穿越的秘密并试图阻止一场灾难";
        String chapterTitle = "第一章：意外的发现";
        String chapterAbstract = "主角在实验室中意外激活了时空装置，首次体验时空穿越。";

        List<String> previousChapters = Arrays.asList(
            "主角加入研究团队，开始接触时空理论",
            "在实验中出现异常现象"
        );

        // 查找测试模型
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("测试模型未找到"));

        String content = aiModelService.generateChapterContent(
                novelTitle, genre, settingText, structure, 
                chapterTitle, chapterAbstract, previousChapters, 3000, model);
        
        assertNotNull(content, "生成的章节内容不应为null");
        assertFalse(content.isEmpty(), "生成的章节内容不应为空");
        System.out.println("=== 生成的章节内容 ===");
        System.out.println(content);
    }

    @Test
    public void testGenerateChapterAbstract() {
        String chapterContent = """
            李明推开实验室的门，心跳加速。眼前的设备发出微弱的蓝光，
            那是他研究了三年的时空装置。突然，一阵强烈的能量波动传来，
            他感到身体被一股无形的力量包围。当光芒散去时，他发现自己
            站在一个完全陌生的地方——古老的长安城。
            """;

        // 查找测试模型
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("测试模型未找到"));

        String abstractContent = aiModelService.generateChapterAbstract(chapterContent, model);
        
        assertNotNull(abstractContent, "生成的章节摘要不应为null");
        assertFalse(abstractContent.isEmpty(), "生成的章节摘要不应为空");
        System.out.println("=== 生成的章节摘要 ===");
        System.out.println(abstractContent);
    }

    @Test
    public void testGetChatModel() {
        // 查找测试模型
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType())
                .orElseThrow(() -> new RuntimeException("测试模型未找到"));

        // 测试获取ChatLanguageModel
        var chatModel = aiModelService.getChatModel(model);
        assertNotNull(chatModel, "ChatLanguageModel不应为null");
    }

    @Test
    public void testCreateChatModel() {
        // 测试创建ChatLanguageModel
        var chatModel = aiModelService.createChatModel(
                "test-key", 
                "https://api.test.com/v1", 
                "test-model");
        assertNotNull(chatModel, "创建的ChatLanguageModel不应为null");
    }

    @Test
    public void testNullInputs() {
        // 测试空值处理
        assertThrows(Exception.class, () -> {
            aiModelService.generateNovelStructure(null, null, null, null, null);
        });
    }

    @Test
    public void testUserModelNotFound() {
        // 测试找不到用户模型的情况
        Optional<UserModel> nonExistentModel = userModelRepository.findByUserIdAndType(999L, 999);
        assertTrue(nonExistentModel.isEmpty(), "应该找不到不存在的用户模型");
    }

    @Test
    public void testMode() {
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType()).orElseThrow(() -> new RuntimeException("测试模型未找到"));
        ChatLanguageModel chatModel = aiModelService.getChatModel(model);
        String message = chatModel.generate("你好");
        System.out.println(message);
    }
}