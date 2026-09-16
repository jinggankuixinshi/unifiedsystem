package com.unified.common.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unified.common.config.WebSocketConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotifier {

    private final ObjectMapper objectMapper;

    @Async
    public void sendToUser(Long userId, String type, Object data) {
        WebSocketSession session = WebSocketConfig.USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", type,
                        "data", data,
                        "timestamp", System.currentTimeMillis()
                );
                String json = objectMapper.writeValueAsString(message);
                if (json.getBytes().length <= 65536) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(json));
                    }
                }
            } catch (IOException e) {
                log.warn("WebSocket 推送失败: userId={}", userId, e);
            }
        }
    }

    @Async
    public void kickUser(Long userId, String reason) {
        WebSocketSession session = WebSocketConfig.USER_SESSIONS.get(userId);
        if (session != null) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "kicked",
                        "data", Map.of("reason", reason),
                        "timestamp", System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                session.close();
            } catch (IOException e) {
                log.warn("踢人消息发送失败: userId={}", userId, e);
            }
        }
    }
}
