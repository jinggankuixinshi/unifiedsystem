package com.unified.common.config;

import com.unified.common.security.JwtUtil;
import com.unified.common.security.TokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtil jwtUtil;
    private final TokenManager tokenManager;

    public static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationHandler(), "/ws/notification")
                .addInterceptors(handshakeInterceptor())
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler notificationHandler() {
        return new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                Long userId = (Long) session.getAttributes().get("userId");
                if (userId != null) {
                    USER_SESSIONS.put(userId, session);
                    log.info("WebSocket 连接建立: userId={}", userId);
                }
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                Long userId = (Long) session.getAttributes().get("userId");
                if (userId != null) {
                    USER_SESSIONS.remove(userId);
                    log.info("WebSocket 连接断开: userId={}", userId);
                }
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, org.springframework.web.socket.TextMessage message) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                           org.springframework.http.server.ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {
                URI uri = request.getURI();
                String query = uri.getQuery();
                if (query == null || !query.contains("token=")) {
                    return false;
                }
                String token = query.substring(query.indexOf("token=") + 6);
                if (!jwtUtil.validateToken(token) || tokenManager.isBlacklisted(token)) {
                    return false;
                }
                Long userId = jwtUtil.getUserIdFromToken(token);
                attributes.put("userId", userId);
                return true;
            }

            @Override
            public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                                       org.springframework.http.server.ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
            }
        };
    }
}
