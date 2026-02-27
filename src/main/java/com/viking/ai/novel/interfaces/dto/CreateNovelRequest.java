package com.viking.ai.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNovelRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String genre;

    private String settingText;

    /** 总章数 */
    private Integer chapterNumber;

    /** 章节字数 */
    private Integer chapterWordCount;

    /** true=异步(MQ)生成结构，false=实时生成；默认 true */
    private Boolean async = true;
}
