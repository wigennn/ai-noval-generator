package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 校验路径中的 userId 必须等于当前登录用户（防越权查他人列表/设置等）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckUserId {
    /** 参数索引（0-based），该参数为 userId，必须等于当前用户 */
    int paramIndex() default 0;
}
