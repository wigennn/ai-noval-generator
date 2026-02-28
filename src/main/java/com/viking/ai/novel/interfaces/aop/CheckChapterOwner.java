package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验章节归属：方法参数中指定索引的值为 chapterId，该章节所属小说必须属于当前用户。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckChapterOwner {
    /** 参数索引（0-based），该参数为章节 ID */
    int paramIndex() default 0;
}
