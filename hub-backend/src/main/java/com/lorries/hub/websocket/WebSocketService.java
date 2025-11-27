package com.lorries.hub.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * WebSocket消息推送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送消息到指定主题
     */
    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }

    /**
     * 推送消息给指定用户
     */
    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    /**
     * 推送车流量数据
     */
    public void pushTrafficFlow(Map<String, Object> trafficData) {
        sendToTopic("traffic/flow", trafficData);
    }

    /**
     * 推送异常告警
     */
    public void pushAnomalyAlert(Map<String, Object> anomalyData) {
        sendToTopic("anomaly/alert", anomalyData);
    }

    /**
     * 推送车辆检测结果
     */
    public void pushVehicleDetection(Map<String, Object> detectionData) {
        sendToTopic("vehicle/detection", detectionData);
    }

    /**
     * 推送停车场状态
     */
    public void pushParkingStatus(Map<String, Object> parkingData) {
        sendToTopic("parking/status", parkingData);
    }

    /**
     * 推送系统通知
     */
    public void pushSystemNotification(Map<String, Object> notification) {
        sendToTopic("system/notification", notification);
    }

    /**
     * 定时推送模拟实时数据（演示用）
     */
    @Scheduled(fixedRate = 5000)
    public void pushRealtimeData() {
        // 模拟车流量数据
        Map<String, Object> trafficData = new HashMap<>();
        trafficData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        trafficData.put("totalVehicles", new Random().nextInt(100) + 50);
        trafficData.put("avgSpeed", 30 + new Random().nextInt(30));
        trafficData.put("congestionLevel", new Random().nextInt(5) + 1);
        
        List<Map<String, Object>> roadData = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> road = new HashMap<>();
            road.put("roadId", i);
            road.put("roadName", "道路" + i);
            road.put("vehicleCount", new Random().nextInt(50) + 10);
            road.put("speed", 25 + new Random().nextInt(35));
            roadData.add(road);
        }
        trafficData.put("roads", roadData);
        
        sendToTopic("traffic/realtime", trafficData);
    }

    /**
     * 定时推送系统状态
     */
    @Scheduled(fixedRate = 10000)
    public void pushSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("cameraOnline", new Random().nextInt(10) + 40);
        status.put("cameraTotal", 50);
        status.put("cpuUsage", 20 + new Random().nextInt(30));
        status.put("memoryUsage", 40 + new Random().nextInt(30));
        status.put("activeAlerts", new Random().nextInt(5));
        
        sendToTopic("system/status", status);
    }
}
