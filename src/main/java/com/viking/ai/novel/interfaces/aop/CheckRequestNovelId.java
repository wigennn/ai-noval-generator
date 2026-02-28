package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验请求体/参数对象中的 novelId 归属：从指定参数（如 CreateChapterRequest）中取 novelId 字段，该小说必须属于当前用户。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckRequestNovelId {
    /** 参数索引（0-based），该参数为包含 novelId 的对象 */
    int paramIndex() default 0;
    /** 对象中表示 novelId 的属性名 */
    String field() default "novelId";
}
