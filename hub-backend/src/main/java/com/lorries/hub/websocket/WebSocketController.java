package com.lorries.hub.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息控制器
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * 接收客户端发送的消息
     */
    @MessageMapping("/message")
    @SendTo("/topic/public")
    public Map<String, Object> handleMessage(@Payload Map<String, Object> message,
                                              SimpMessageHeaderAccessor headerAccessor) {
        log.info("Received message: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "message");
        response.put("content", message);
        response.put("sessionId", headerAccessor.getSessionId());
        
        return response;
    }

    /**
     * 订阅车流量数据
     */
    @MessageMapping("/subscribe/traffic")
    @SendTo("/topic/traffic/realtime")
    public Map<String, Object> subscribeTraffic(@Payload Map<String, Object> subscription) {
        log.info("Traffic subscription: {}", subscription);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "subscribed");
        response.put("channel", "traffic/realtime");
        
        return response;
    }

    /**
     * 订阅异常告警
     */
    @MessageMapping("/subscribe/anomaly")
    @SendTo("/topic/anomaly/alert")
    public Map<String, Object> subscribeAnomaly(@Payload Map<String, Object> subscription) {
        log.info("Anomaly subscription: {}", subscription);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "subscribed");
        response.put("channel", "anomaly/alert");
        
        return response;
    }

    /**
     * 触发手动刷新数据
     */
    @MessageMapping("/refresh")
    public void refreshData(@Payload Map<String, Object> request) {
        String dataType = (String) request.getOrDefault("type", "all");
        log.info("Manual refresh requested for: {}", dataType);
        
        // 根据请求类型推送对应数据
        switch (dataType) {
            case "traffic":
                webSocketService.pushRealtimeData();
                break;
            case "system":
                webSocketService.pushSystemStatus();
                break;
            default:
                webSocketService.pushRealtimeData();
                webSocketService.pushSystemStatus();
        }
    }
}
