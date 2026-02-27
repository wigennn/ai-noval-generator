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
    public void testMode() {
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType()).orElseThrow(() -> new RuntimeException("测试模型未找到"));
        ChatLanguageModel chatModel = aiModelService.getChatModel(model);
        String message = chatModel.generate("你好");
        System.out.println(message);
    }
}