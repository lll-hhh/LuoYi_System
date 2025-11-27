package com.lorries.mobile.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知WebSocket处理器
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("通知WebSocket连接建立: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            if ("subscribe".equals(type)) {
                Long userId = ((Number) data.get("userId")).longValue();
                subscribeUser(userId, session.getId());
                sendMessage(session, createResponse("subscribed", userId));
            } else if ("unsubscribe".equals(type)) {
                Long userId = ((Number) data.get("userId")).longValue();
                unsubscribeUser(userId, session.getId());
            }
        } catch (Exception e) {
            logger.error("处理通知消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        userSessions.values().forEach(sessionIds -> sessionIds.remove(sessionId));
        
        logger.info("通知WebSocket连接关闭: {}", sessionId);
    }

    private void subscribeUser(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    private void unsubscribeUser(Long userId, String sessionId) {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds != null) {
            sessionIds.remove(sessionId);
        }
    }

    /**
     * 发送通知给用户
     */
    public void sendNotification(Long userId, String title, String content, String notificationType) {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds != null) {
            String message = createNotification(title, content, notificationType);
            sessionIds.forEach(sessionId -> {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        sendMessage(session, message);
                    } catch (IOException e) {
                        logger.error("发送通知失败: {}", e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 广播通知给所有用户
     */
    public void broadcastNotification(String title, String content, String notificationType) {
        String message = createNotification(title, content, notificationType);
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    logger.error("广播通知失败: {}", e.getMessage());
                }
            }
        });
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    private String createNotification(String title, String content, String type) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "notification");
            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("content", content);
            data.put("notificationType", type);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String createResponse(String type, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{}";
        }
    }
}
