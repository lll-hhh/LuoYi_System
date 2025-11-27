package com.lorries.hub.service;

import com.lorries.hub.config.RabbitMQConfig;
import com.lorries.hub.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ消息消费者服务
 * 处理来自消息队列的各类事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumerService {

    private final WebSocketService webSocketService;
    private final AnomalyService anomalyService;
    private final TrafficFlowService trafficFlowService;

    /**
     * 处理交通流量消息
     */
    @RabbitListener(queues = RabbitMQConfig.TRAFFIC_FLOW_QUEUE)
    public void handleTrafficFlowMessage(Map<String, Object> message) {
        log.info("收到交通流量消息: {}", message);
        try {
            // 推送到WebSocket
            webSocketService.pushTrafficFlow(message);
            
        } catch (Exception e) {
            log.error("处理交通流量消息失败", e);
        }
    }

    /**
     * 处理异常检测消息
     */
    @RabbitListener(queues = RabbitMQConfig.ANOMALY_DETECTION_QUEUE)
    public void handleAnomalyMessage(Map<String, Object> message) {
        log.info("收到异常检测消息: {}", message);
        try {
            // 推送告警到WebSocket
            webSocketService.pushAnomalyAlert(message);
            
        } catch (Exception e) {
            log.error("处理异常检测消息失败", e);
        }
    }

    /**
     * 处理任务通知消息
     */
    @RabbitListener(queues = RabbitMQConfig.TASK_NOTIFICATION_QUEUE)
    public void handleTaskNotification(Map<String, Object> message) {
        log.info("收到任务通知消息: {}", message);
        try {
            // 推送系统通知
            webSocketService.pushSystemNotification(message);
            
        } catch (Exception e) {
            log.error("处理任务通知消息失败", e);
        }
    }
}
