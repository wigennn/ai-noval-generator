package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.domain.model.UserModel;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import com.viking.ai.novel.domain.repository.TaskRepository;
import com.viking.ai.novel.domain.repository.UserModelRepository;
import com.viking.ai.novel.infrastructure.ai.QdrantService;
import com.viking.ai.novel.infrastructure.constants.ModelTypeEnum;
import com.viking.ai.novel.infrastructure.mq.AiGenerateProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 章节应用服务
 */
@Service
@Slf4j
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final TaskRepository taskRepository;
    private final ChapterGenerationTaskService chapterGenerationTaskService;
    private final QdrantService qdrantService;
    private final AiGenerateProducer aiGenerateProducer;
    private final UserModelRepository userModelRepository;

    public ChapterService(ChapterRepository chapterRepository,
                          NovelRepository novelRepository,
                          TaskRepository taskRepository,
                          ChapterGenerationTaskService chapterGenerationTaskService,
                          QdrantService qdrantService,
                          @Autowired(required = false) AiGenerateProducer aiGenerateProducer,
                          UserModelRepository userModelRepository) {
        this.chapterRepository = chapterRepository;
        this.novelRepository = novelRepository;
        this.taskRepository = taskRepository;
        this.chapterGenerationTaskService = chapterGenerationTaskService;
        this.qdrantService = qdrantService;
        this.aiGenerateProducer = aiGenerateProducer;
        this.userModelRepository = userModelRepository;
    }

    /**
     * 生成章节
     *
     * @param async true=通过 MQ 异步生成，false=同步生成（阻塞至完成）
     */
    @Transactional
    public Chapter generateChapter(Long novelId, Integer chapterNumber, String chapterTitle, String chapterAbstract, boolean async) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));

        Optional<Chapter> existingChapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (existingChapter.isPresent()) {
            throw new RuntimeException("Chapter already exists: " + chapterNumber);
        }

        Chapter chapter = Chapter.builder()
                .novelId(novelId)
                .chapterNumber(chapterNumber)
                .title(chapterTitle)
                .abstractContent(chapterAbstract)
                .status(0)
                .build();
        chapter = chapterRepository.save(chapter);

        Task task = Task.builder()
                .taskName("生成章节内容")
                .taskType("GENERATE_CHAPTER")
                .taskRelationId(chapter.getId())
                .taskStatus(0)
                .build();
        taskRepository.save(task);

        if (async && aiGenerateProducer != null) {
            aiGenerateProducer.sendChapterContent(novel.getId(), chapter.getId(), task.getId());
        } else {
            chapterGenerationTaskService.doGenerateChapterContent(novel.getId(), chapter.getId(), task.getId());
        }
        return chapter;
    }

    /**
     * 根据ID获取章节
     */
    public Optional<Chapter> getChapterById(Long id) {
        return chapterRepository.findById(id);
    }

    /**
     * 获取小说的所有章节
     */
    public List<Chapter> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findByNovelId(novelId);
    }

    /**
     * 更新章节
     */
    @Transactional
    public Chapter updateChapter(Long id, String title, String content) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));
        Novel novel = novelRepository.findById(chapter.getNovelId())
                .orElseThrow(() -> new RuntimeException("Novel not found: " + chapter.getNovelId()));
        UserModel model = userModelRepository.findByUserIdAndType(novel.getUserId(), ModelTypeEnum.VECTOR.getType())
                .orElseThrow(() -> new RuntimeException("User model not found: " + novel.getUserId()));

        if (title != null) {
            chapter.setTitle(title);
        }
        if (content != null) {
            chapter.setContent(content);
            // 更新向量数据库
            if (chapter.getVectorId() != null) {
                qdrantService.deleteVector(chapter.getVectorId());
            }
            String vectorId = qdrantService.storeChapter(chapter.getId().toString(), content, model);
            chapter.setVectorId(vectorId);
        }

        return chapterRepository.save(chapter);
    }

    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + id));

        // 删除向量数据库中的内容
        if (chapter.getVectorId() != null) {
            qdrantService.deleteVector(chapter.getVectorId());
        }

        chapterRepository.deleteById(id);
    }

    /**
     * 删除小说的所有章节（用于重新生成章节大纲时）
     */
    @Transactional
    public void deleteChaptersByNovelId(Long novelId) {
        List<Chapter> chapters = chapterRepository.findByNovelId(novelId);
        for (Chapter chapter : chapters) {
            // 删除向量数据库中的内容
            if (chapter.getVectorId() != null) {
                qdrantService.deleteVector(chapter.getVectorId());
            }
        }
        chapterRepository.deleteByNovelId(novelId);
    }

    /**
     * 从章节大纲中提取每章标题，同步到 chapters 表
     */
    @Transactional
    public void syncChaptersFromOutline(Long novelId, String outline) {
        if (outline == null || outline.isEmpty()) {
            return;
        }
        // 匹配格式：## 第X章 [章节标题] 或 ## 第X章 章节标题
        Pattern pattern = Pattern.compile("^##\\s*第([一二三四五六七八九十百千万]+|\\d+)章\\s*(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(outline);

        while (matcher.find()) {
            String chapterNumStr = matcher.group(1);
            int chapterNumber;

            // 判断是否为中文数字并转换
            if (chapterNumStr.matches("[一二三四五六七八九十百千万]+")) {
                chapterNumber = convertChineseToArabic(chapterNumStr);
            } else {
                chapterNumber = Integer.parseInt(chapterNumStr);
            }

            String rawTitle = matcher.group(2) != null ? matcher.group(2).trim() : "";
            // 去掉可能的方括号
            String title = rawTitle.replaceAll("^[\\[【]?|[】\\]]?$", "").trim();

            Optional<Chapter> existingOpt = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
            if (existingOpt.isPresent()) {
                // 已存在章节，不强制覆盖标题，只在标题为空时补充
                Chapter existing = existingOpt.get();
                if ((existing.getTitle() == null || existing.getTitle().isEmpty()) && !title.isEmpty()) {
                    existing.setTitle(title);
                    chapterRepository.save(existing);
                }
            } else {
                // 创建占位章节记录（仅有编号和标题，状态为待处理）
                Chapter chapter = Chapter.builder()
                        .novelId(novelId)
                        .chapterNumber(chapterNumber)
                        .title(title.isEmpty() ? null : title)
                        .status(0)
                        .build();
                chapterRepository.save(chapter);
            }
        }
    }

    private int convertChineseToArabic(String chineseNum) {
        Map<Character, Integer> map = new HashMap<>() {{
            put('一', 1);
            put('二', 2);
            put('三', 3);
            put('四', 4);
            put('五', 5);
            put('六', 6);
            put('七', 7);
            put('八', 8);
            put('九', 9);
            put('十', 10);
            put('百', 100);
            put('千', 1000);
            put('万', 10000);
        }};


        int result = 0;
        int temp = 0;
        for (char c : chineseNum.toCharArray()) {
            int value = map.getOrDefault(c, 0);
            if (value >= 10) {
                if (temp == 0) temp = 1;
                result += temp * value;
                temp = 0;
            } else {
                temp = temp * 10 + value;
            }
        }
        result += temp;
        return result;
    }


}
