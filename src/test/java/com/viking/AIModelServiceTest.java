package com.viking;

import com.viking.ai.novel.NovelGeneratorApplication;
import com.viking.ai.novel.application.service.ChapterService;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.ai.AiModelService;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import com.viking.ai.novel.infrastructure.utils.BasicUtils;
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
    private ChapterService chapterService;
    
    @Autowired
    private UserModelRepository userModelRepository;
    @Autowired
    private NovelRepository novelRepository;

    @Test
    public void testMode() {
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.NORMAL.getType()).orElseThrow(() -> new RuntimeException("测试模型未找到"));
        ChatLanguageModel chatModel = aiModelService.getChatModel(model);
        String message = chatModel.generate("你好");
        System.out.println(message);
    }

    @Test
    public void testchap() {
        Novel novel = novelRepository.findById(4L).orElseThrow(() -> new RuntimeException("小说未找到"));
        chapterService.syncChaptersFromOutline(novel.getId(), novel.getChapterOutline());
    }

    @Autowired
    private QdrantService qdrantService;

    @Test
    public void testEmbeddingMode() {
        UserModel model = userModelRepository.findByUserIdAndType(1L, ModelTypeEnum.VECTOR.getType()).orElseThrow();
        String content = "边军斥候陆沉驻守的铁岩镇遭遇“征灵日”，云岚宗修士凌霄子前来抽取有灵根孩童的灵根。陆沉的妹妹小雨被选中，其母反抗被杀。陆沉率弟兄以毒弩反抗，却遭凌霄子弹指间屠戮全军，铁岩镇化为血海。重伤坠河的陆沉被神秘老者墨衡所救。墨衡自称研究“斩仙”之法，看中陆沉濒死而不灭的恨意与坚韧。家园尽毁、至亲被夺的陆沉，在绝望中抓住复仇的微光，跟随墨衡踏入黑暗的洞窟，誓要以凡人之躯，向仙神挥刃。";
        String vectorId = qdrantService.storeText(content, model, BasicUtils.getCollectionName(1L, null));
        System.out.println(vectorId);
    }
}