package com.viking.ai.novel.infrastructure.config;

import java.security.Principal;

/**
 * STOMP WebSocket 会话中的用户主体，name 为 userId 字符串
 */
public class StompPrincipal implements Principal {
    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
