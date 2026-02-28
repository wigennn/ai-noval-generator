package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验小说向量关联归属：方法参数中指定索引的值为 novelVectorId，对应小说必须属于当前用户。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckNovelVectorOwner {
    int paramIndex() default 0;
}
