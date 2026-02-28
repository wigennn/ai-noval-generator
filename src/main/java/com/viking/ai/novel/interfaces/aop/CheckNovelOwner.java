package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验小说归属：方法参数中指定索引的值为 novelId，该小说必须属于当前用户。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckNovelOwner {
    /** 参数索引（0-based），该参数为小说 ID */
    int paramIndex() default 0;
}
