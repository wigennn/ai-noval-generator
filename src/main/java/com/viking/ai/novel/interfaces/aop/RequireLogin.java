package com.viking.ai.novel.interfaces.aop;

import java.lang.annotation.*;

/**
 * 要求当前请求已登录，否则抛出 NotLoggedInException（全局异常处理映射为 401）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
