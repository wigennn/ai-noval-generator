package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验小说 ID 归属：方法参数中指定索引的值为 novelId，该小说必须属于当前用户。
 * 用于入参为 novelId 的接口（如章节列表、导出等）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckNovelIdOwner {
    int paramIndex() default 0;
}
