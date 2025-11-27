package com.lorries.hub.service;

import com.lorries.hub.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ消息生产者服务
 * 负责发送各类消息到消息队列
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送交通流量数据
     */
    public void sendTrafficFlowData(Map<String, Object> trafficData) {
        trafficData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TRAFFIC_EXCHANGE,
            RabbitMQConfig.TRAFFIC_FLOW_KEY,
            trafficData
        );
        log.debug("发送交通流量数据: {}", trafficData);
    }

    /**
     * 发送异常检测结果
     */
    public void sendAnomalyDetection(String anomalyType, String location, String description, String severity) {
        Map<String, Object> anomalyData = new HashMap<>();
        anomalyData.put("type", anomalyType);
        anomalyData.put("location", location);
        anomalyData.put("description", description);
        anomalyData.put("severity", severity);
        anomalyData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ANOMALY_EXCHANGE,
            RabbitMQConfig.ANOMALY_DETECTION_KEY,
            anomalyData
        );
        log.info("发送异常检测消息: {}", anomalyData);
    }

    /**
     * 发送任务通知
     */
    public void sendTaskNotification(Long taskId, String taskType, String title, String content) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("taskId", taskId);
        notification.put("taskType", taskType);
        notification.put("title", title);
        notification.put("content", content);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TASK_EXCHANGE,
            RabbitMQConfig.TASK_NOTIFICATION_KEY,
            notification
        );
        log.info("发送任务通知: {}", notification);
    }

    /**
     * 发送车辆过境信息
     */
    public void sendVehiclePassage(String plateNumber, Long cameraId, String direction) {
        Map<String, Object> passage = new HashMap<>();
        passage.put("plateNumber", plateNumber);
        passage.put("cameraId", cameraId);
        passage.put("direction", direction);
        passage.put("passTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TRAFFIC_EXCHANGE,
            RabbitMQConfig.TRAFFIC_FLOW_KEY,
            passage
        );
        log.debug("发送车辆过境信息: {}", passage);
    }

    /**
     * 发送拥堵预警
     */
    public void sendCongestionWarning(Integer roadId, String roadName, double congestionIndex, String suggestion) {
        Map<String, Object> warning = new HashMap<>();
        warning.put("roadId", roadId);
        warning.put("roadName", roadName);
        warning.put("congestionIndex", congestionIndex);
        warning.put("suggestion", suggestion);
        warning.put("level", congestionIndex >= 6 ? "严重" : congestionIndex >= 4 ? "中等" : "轻微");
        warning.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ANOMALY_EXCHANGE,
            RabbitMQConfig.ANOMALY_DETECTION_KEY,
            warning
        );
        log.info("发送拥堵预警: {}", warning);
    }

    /**
     * 发送设备状态变更
     */
    public void sendDeviceStatusChange(Long deviceId, String deviceType, String oldStatus, String newStatus) {
        Map<String, Object> statusChange = new HashMap<>();
        statusChange.put("deviceId", deviceId);
        statusChange.put("deviceType", deviceType);
        statusChange.put("oldStatus", oldStatus);
        statusChange.put("newStatus", newStatus);
        statusChange.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TASK_EXCHANGE,
            RabbitMQConfig.TASK_NOTIFICATION_KEY,
            statusChange
        );
        log.info("发送设备状态变更: {}", statusChange);
    }
}
