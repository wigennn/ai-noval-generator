package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验用户向量归属：方法参数中指定索引的值为 userVectorId，该向量必须属于当前用户。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckUserVectorOwner {
    int paramIndex() default 0;
}
