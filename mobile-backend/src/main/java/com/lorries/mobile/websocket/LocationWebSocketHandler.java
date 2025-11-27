package com.lorries.mobile.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorries.mobile.dto.LocationReportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 位置WebSocket处理器
 */
@Component
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocationWebSocketHandler.class);

    // 存储所有连接的会话
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 存储用户ID和会话的映射
    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();

    // 存储会话角色：driver/admin
    private final Map<String, String> sessionRoles = new ConcurrentHashMap<>();

    private final java.util.Set<String> adminSessions = ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("WebSocket连接建立: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("收到消息: {}", payload);

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            switch (type) {
                case "auth":
                    handleAuth(session, data);
                    break;
                case "location":
                    handleLocation(session, data);
                    break;
                case "ping":
                    sendMessage(session, createMessage("pong", null));
                    break;
                default:
                    logger.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            logger.error("处理消息失败: {}", e.getMessage());
            sendMessage(session, createMessage("error", Collections.singletonMap("message", e.getMessage())));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        sessionRoles.remove(sessionId);
        adminSessions.remove(sessionId);
        
        // 移除用户映射
        userSessionMap.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        
        logger.info("WebSocket连接关闭: {}, 状态: {}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket传输错误: {}", exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    /**
     * 处理认证
     */
    private void handleAuth(WebSocketSession session, Map<String, Object> data) throws IOException {
        Long userId = ((Number) data.get("userId")).longValue();
        String sessionId = session.getId();
        String clientType = "driver";
        Object clientTypeValue = data.get("clientType");
        if (clientTypeValue != null) {
            clientType = clientTypeValue.toString().toLowerCase();
        }

        sessionRoles.put(sessionId, clientType);
        if (isAdminClient(clientType)) {
            adminSessions.add(sessionId);
        } else {
            adminSessions.remove(sessionId);
        }

        userSessionMap.put(userId, sessionId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("clientType", clientType);
        sendMessage(session, createMessage("auth_success", payload));
        logger.info("用户认证成功: {} ({})", userId, clientType);
    }

    /**
     * 处理位置更新
     */
    private void handleLocation(WebSocketSession session, Map<String, Object> data) throws IOException {
        LocationReportRequest location = objectMapper.convertValue(data.get("data"), LocationReportRequest.class);
        
        // 广播位置更新给管理端
    Map<String, Object> payload = new HashMap<>();
    payload.put("vehicleId", data.get("vehicleId"));
    payload.put("location", location);
    broadcastToAdmins(createMessage("location_update", payload));
        
        sendMessage(session, createMessage("location_ack", null));
    }

    /**
     * 向用户发送消息
     */
    public void sendToUser(Long userId, String message) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId != null) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    logger.error("发送消息失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 广播消息给所有管理员
     */
    public void broadcastToAdmins(String message) {
        if (adminSessions.isEmpty()) {
            logger.debug("无管理员在线，广播消息给所有会话");
            broadcast(message);
            return;
        }
        adminSessions.forEach(sessionId -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    logger.error("推送管理员消息失败: {}", e.getMessage());
                }
            }
        });
    }

    private boolean isAdminClient(String clientType) {
        if (clientType == null) {
            return false;
        }
        switch (clientType.toLowerCase()) {
            case "admin":
            case "monitor":
            case "dispatcher":
                return true;
            default:
                return false;
        }
    }

    /**
     * 广播消息给所有连接
     */
    public void broadcast(String message) {
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    logger.error("广播消息失败: {}", e.getMessage());
                }
            }
        });
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    private String createMessage(String type, Object data) {
        try {
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", type);
            if (data != null) {
                message.put("data", data);
            }
            message.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"消息序列化失败\"}";
        }
    }

    public int getOnlineCount() {
        return sessions.size();
    }
}
