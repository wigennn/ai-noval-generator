package com.viking.ai.novel.application.service;

import com.viking.ai.novel.infrastructure.excep.NotLoggedInException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 当前登录用户解析（与 AuthController 使用同一 Session 键）
 */
@Service
@Slf4j
public class CurrentUserService {

    public static final String SESSION_USER_ID = "userId";

    /**
     * 获取当前登录用户 ID，未登录返回 empty
     */
    public Optional<Long> getCurrentUserId(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }
        Object attr = session.getAttribute(SESSION_USER_ID);
        if (attr instanceof Long) {
            return Optional.of((Long) attr);
        }
        if (attr instanceof Number) {
            return Optional.of(((Number) attr).longValue());
        }
        return Optional.empty();
    }

    /**
     * 要求已登录，返回当前用户 ID；未登录抛出 RuntimeException（由上层映射为 401）
     */
    public long requireCurrentUserId(HttpSession session) {
        return getCurrentUserId(session)
                .orElseThrow(() -> new NotLoggedInException("未登录"));
    }
}
