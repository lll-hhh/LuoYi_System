package com.lorries.hub.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息推送服务
 * 提供实时数据推送、用户订阅管理等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    
    // 用户订阅管理
    private final Map<String, Set<String>> userSubscriptions = new ConcurrentHashMap<>();
    
    // 活跃连接计数
    private final Map<String, Integer> activeConnections = new ConcurrentHashMap<>();

    /**
     * 推送消息到指定主题
     */
    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
        log.debug("推送消息到主题: {}", topic);
    }

    /**
     * 推送消息给指定用户
     */
    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
        log.debug("推送消息给用户: {} -> {}", username, destination);
    }
    
    /**
     * 广播消息给所有连接
     */
    public void broadcast(Object message) {
        sendToTopic("broadcast", message);
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
        // 同时推送给订阅了告警的用户
        notifySubscribedUsers("anomaly", anomalyData);
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
     * 推送拥堵预警
     */
    public void pushCongestionWarning(Map<String, Object> congestionData) {
        sendToTopic("traffic/congestion", congestionData);
    }
    
    /**
     * 推送路口信号灯状态
     */
    public void pushSignalStatus(Map<String, Object> signalData) {
        sendToTopic("signal/status", signalData);
    }
    
    /**
     * 推送设备状态变化
     */
    public void pushDeviceStatus(Map<String, Object> deviceData) {
        sendToTopic("device/status", deviceData);
    }
    
    /**
     * 用户订阅主题
     */
    public void subscribe(String username, String topic) {
        userSubscriptions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(topic);
        log.info("用户 {} 订阅主题: {}", username, topic);
    }
    
    /**
     * 用户取消订阅
     */
    public void unsubscribe(String username, String topic) {
        Set<String> topics = userSubscriptions.get(username);
        if (topics != null) {
            topics.remove(topic);
            log.info("用户 {} 取消订阅: {}", username, topic);
        }
    }
    
    /**
     * 通知订阅了特定主题的用户
     */
    private void notifySubscribedUsers(String topic, Object message) {
        userSubscriptions.forEach((username, topics) -> {
            if (topics.contains(topic)) {
                sendToUser(username, "/queue/" + topic, message);
            }
        });
    }
    
    /**
     * 记录用户连接
     */
    public void onUserConnect(String sessionId, String username) {
        activeConnections.merge(username, 1, Integer::sum);
        log.info("用户连接: {} (sessionId: {}), 当前连接数: {}", 
            username, sessionId, activeConnections.get(username));
    }
    
    /**
     * 记录用户断开
     */
    public void onUserDisconnect(String sessionId, String username) {
        activeConnections.computeIfPresent(username, (k, v) -> v > 1 ? v - 1 : null);
        log.info("用户断开: {} (sessionId: {})", username, sessionId);
    }
    
    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return activeConnections.size();
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
        String[] roadNames = {"中山路", "人民大道", "建设路", "解放大道", "和平路"};
        for (int i = 0; i < roadNames.length; i++) {
            Map<String, Object> road = new HashMap<>();
            road.put("roadId", i + 1);
            road.put("roadName", roadNames[i]);
            road.put("vehicleCount", new Random().nextInt(500) + 200);
            road.put("speed", 25 + new Random().nextInt(35));
            road.put("congestionIndex", 1.0 + new Random().nextDouble() * 5);
            roadData.add(road);
        }
        trafficData.put("roads", roadData);
        
        sendToTopic("traffic/realtime", trafficData);
        
        // 偶尔推送异常告警
        if (new Random().nextInt(10) == 0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", UUID.randomUUID().toString());
            alert.put("type", new String[]{"超速", "违停", "拥堵", "设备故障"}[new Random().nextInt(4)]);
            alert.put("location", roadNames[new Random().nextInt(roadNames.length)]);
            alert.put("level", new String[]{"低", "中", "高"}[new Random().nextInt(3)]);
            alert.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            alert.put("description", "系统检测到异常情况");
            
            pushAnomalyAlert(alert);
        }
    }

    /**
     * 定时推送系统状态
     */
    @Scheduled(fixedRate = 10000)
    public void pushSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("cameraOnline", new Random().nextInt(10) + 150);
        status.put("cameraTotal", 168);
        status.put("cpuUsage", 20 + new Random().nextInt(30));
        status.put("memoryUsage", 40 + new Random().nextInt(30));
        status.put("activeAlerts", new Random().nextInt(10));
        status.put("onlineUsers", getOnlineUserCount());
        status.put("algorithmService", "healthy");
        
        sendToTopic("system/status", status);
    }
    
    /**
     * 定时推送拥堵预警（高峰时段更频繁）
     */
    @Scheduled(fixedRate = 30000)
    public void pushCongestionAlert() {
        int hour = LocalDateTime.now().getHour();
        // 早晚高峰时段
        boolean isPeakHour = (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
        
        if (isPeakHour || new Random().nextInt(3) == 0) {
            Map<String, Object> congestion = new HashMap<>();
            congestion.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            congestion.put("isPeakHour", isPeakHour);
            
            List<Map<String, Object>> congestionRoads = new ArrayList<>();
            String[] roadNames = {"中山路", "人民大道", "建设路"};
            for (String roadName : roadNames) {
                Map<String, Object> road = new HashMap<>();
                road.put("roadName", roadName);
                road.put("congestionIndex", isPeakHour ? 4.0 + new Random().nextDouble() * 4 : 2.0 + new Random().nextDouble() * 3);
                road.put("prediction", isPeakHour ? "预计30分钟后缓解" : "路况正常");
                congestionRoads.add(road);
            }
            congestion.put("roads", congestionRoads);
            
            pushCongestionWarning(congestion);
        }
    }
}
