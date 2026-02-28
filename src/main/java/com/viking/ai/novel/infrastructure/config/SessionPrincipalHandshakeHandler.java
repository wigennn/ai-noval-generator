package com.viking.ai.novel.infrastructure.config;

import com.viking.ai.novel.application.service.CurrentUserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 从握手属性中读取 userId，设置为 STOMP Principal，以便 @MessageMapping 中校验身份
 */
public class SessionPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object userId = attributes.get(CurrentUserService.SESSION_USER_ID);
        if (userId instanceof Long) {
            return new StompPrincipal(((Long) userId).toString());
        }
        if (userId instanceof Number) {
            return new StompPrincipal(((Number) userId).longValue() + "");
        }
        return null;
    }
}
