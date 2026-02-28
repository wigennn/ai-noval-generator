package com.viking.ai.novel.infrastructure.config;

import com.viking.ai.novel.application.service.CurrentUserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * 将 HttpSession 中的 userId 放入 WebSocket 握手属性，供 HandshakeHandler 使用
 */
public class SessionUserIdHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpSession session = ((ServletServerHttpRequest) request).getServletRequest().getSession(false);
            if (session != null) {
                Object userId = session.getAttribute(CurrentUserService.SESSION_USER_ID);
                if (userId != null) {
                    attributes.put(CurrentUserService.SESSION_USER_ID, userId);
                }
            }
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}
