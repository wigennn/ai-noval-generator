package com.viking.ai.novel.application.service;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.domain.repository.ChapterRepository;
import com.viking.ai.novel.domain.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

/**
 * 小说章节导出：TXT、Markdown、Word (.docx)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NovelExportService {

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;

    public enum ExportFormat {
        TXT("txt", "text/plain; charset=UTF-8"),
        MD("md", "text/markdown; charset=UTF-8"),
        DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        private final String extension;
        private final String contentType;

        ExportFormat(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public String getExtension() {
            return extension;
        }

        public String getContentType() {
            return contentType;
        }

        public static ExportFormat from(String format) {
            if (format == null) return TXT;
            String f = format.trim().toLowerCase();
            return switch (f) {
                case "md", "markdown" -> MD;
                case "docx", "word" -> DOCX;
                default -> TXT;
            };
        }
    }

    /**
     * 导出小说为指定格式，返回可下载的 Resource 及建议文件名
     */
    public ExportResult export(Long novelId, ExportFormat format) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found: " + novelId));
        List<Chapter> chapters = chapterRepository.findByNovelId(novelId);
        chapters.sort(Comparator.comparing(Chapter::getChapterNumber));

        byte[] bytes = switch (format) {
            case TXT -> buildTxt(novel, chapters);
            case MD -> buildMarkdown(novel, chapters);
            case DOCX -> buildDocx(novel, chapters);
        };

        String safeTitle = sanitizeFileName(novel.getTitle());
        String filename = safeTitle + "." + format.getExtension();
        Resource resource = new ByteArrayResource(bytes);
        return new ExportResult(resource, filename, format.getContentType());
    }

    private byte[] buildTxt(Novel novel, List<Chapter> chapters) {
        StringBuilder sb = new StringBuilder();
        sb.append(novel.getTitle()).append("\n\n");
        for (Chapter ch : chapters) {
            sb.append("第 ").append(ch.getChapterNumber()).append(" 章");
            if (ch.getTitle() != null && !ch.getTitle().isBlank()) {
                sb.append(" ").append(ch.getTitle());
            }
            sb.append("\n\n");
            if (ch.getContent() != null) {
                sb.append(ch.getContent().trim()).append("\n\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildMarkdown(Novel novel, List<Chapter> chapters) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(escapeMd(novel.getTitle())).append("\n\n");
        for (Chapter ch : chapters) {
            sb.append("## 第 ").append(ch.getChapterNumber()).append(" 章");
            if (ch.getTitle() != null && !ch.getTitle().isBlank()) {
                sb.append(" ").append(escapeMd(ch.getTitle()));
            }
            sb.append("\n\n");
            if (ch.getContent() != null) {
                sb.append(ch.getContent().trim().replace("\n", "\n\n")).append("\n\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeMd(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("#", "\\#").replace("*", "\\*");
    }

    private byte[] buildDocx(Novel novel, List<Chapter> chapters) {
        try (XWPFDocument doc = new XWPFDocument()) {
            // 书名
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(novel.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(22);
            titleRun.setFontFamily("宋体");

            doc.createParagraph();

            for (Chapter ch : chapters) {
                // 章节标题
                String chapterTitle = "第 " + ch.getChapterNumber() + " 章" +
                        (ch.getTitle() != null && !ch.getTitle().isBlank() ? " " + ch.getTitle() : "");
                XWPFParagraph chPara = doc.createParagraph();
                XWPFRun chRun = chPara.createRun();
                chRun.setText(chapterTitle);
                chRun.setBold(true);
                chRun.setFontSize(14);
                chRun.setFontFamily("宋体");

                if (ch.getContent() != null && !ch.getContent().isBlank()) {
                    String content = ch.getContent().trim();
                    for (String block : content.split("\\n\\n+")) {
                        if (block.isBlank()) continue;
                        XWPFParagraph contentPara = doc.createParagraph();
                        contentPara.setAlignment(ParagraphAlignment.BOTH);
                        XWPFRun contentRun = contentPara.createRun();
                        contentRun.setText(block.replace("\n", " "));
                        contentRun.setFontSize(12);
                        contentRun.setFontFamily("宋体");
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to build docx", e);
            throw new RuntimeException("生成 Word 文档失败", e);
        }
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "novel";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    public record ExportResult(Resource resource, String filename, String contentType) {}
}
