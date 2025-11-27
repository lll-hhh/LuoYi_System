package com.lorries.hub.controller;

import com.lorries.hub.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 告警管理控制器
 * 接收Alertmanager的webhook告警并进行处理
 */
@Slf4j
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Tag(name = "告警管理", description = "系统告警接收与管理接口")
public class AlertController {

    private final SimpMessagingTemplate messagingTemplate;

    // 内存中存储最近的告警（生产环境应使用数据库）
    private final ConcurrentLinkedQueue<AlertRecord> recentAlerts = new ConcurrentLinkedQueue<>();
    private static final int MAX_ALERTS = 1000;

    @PostMapping("/webhook")
    @Operation(summary = "接收Alertmanager告警", description = "处理Alertmanager发送的告警webhook")
    public ApiResponse<Void> receiveAlert(@RequestBody AlertmanagerPayload payload) {
        log.info("收到Alertmanager告警: {} 条", payload.getAlerts().size());

        for (Alert alert : payload.getAlerts()) {
            // 创建告警记录
            AlertRecord record = new AlertRecord();
            record.setAlertName(alert.getLabels().getOrDefault("alertname", "Unknown"));
            record.setSeverity(alert.getLabels().getOrDefault("severity", "info"));
            record.setInstance(alert.getLabels().getOrDefault("instance", ""));
            record.setJob(alert.getLabels().getOrDefault("job", ""));
            record.setStatus(alert.getStatus());
            record.setSummary(alert.getAnnotations().getOrDefault("summary", ""));
            record.setDescription(alert.getAnnotations().getOrDefault("description", ""));
            record.setStartsAt(alert.getStartsAt());
            record.setEndsAt(alert.getEndsAt());
            record.setReceivedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 保存到内存队列
            if (recentAlerts.size() >= MAX_ALERTS) {
                recentAlerts.poll();
            }
            recentAlerts.offer(record);

            // 通过WebSocket推送到前端
            pushAlertToWebSocket(record);

            // 根据严重程度进行不同处理
            handleAlertBySeverity(record);
        }

        return ApiResponse.success(null);
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近的告警", description = "获取最近接收到的告警列表")
    public ApiResponse<List<AlertRecord>> getRecentAlerts(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status) {
        
        List<AlertRecord> result = new ArrayList<>();
        for (AlertRecord alert : recentAlerts) {
            if (severity != null && !severity.equals(alert.getSeverity())) {
                continue;
            }
            if (status != null && !status.equals(alert.getStatus())) {
                continue;
            }
            result.add(alert);
            if (result.size() >= limit) {
                break;
            }
        }
        
        return ApiResponse.success(result);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取告警统计", description = "获取告警的统计信息")
    public ApiResponse<Map<String, Object>> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int total = recentAlerts.size();
        int critical = 0;
        int warning = 0;
        int info = 0;
        int firing = 0;
        int resolved = 0;
        
        for (AlertRecord alert : recentAlerts) {
            switch (alert.getSeverity()) {
                case "critical":
                    critical++;
                    break;
                case "warning":
                    warning++;
                    break;
                default:
                    info++;
            }
            
            if ("firing".equals(alert.getStatus())) {
                firing++;
            } else {
                resolved++;
            }
        }
        
        stats.put("total", total);
        stats.put("critical", critical);
        stats.put("warning", warning);
        stats.put("info", info);
        stats.put("firing", firing);
        stats.put("resolved", resolved);
        
        return ApiResponse.success(stats);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空告警记录", description = "清空内存中的告警记录")
    public ApiResponse<Void> clearAlerts() {
        recentAlerts.clear();
        return ApiResponse.success(null);
    }

    /**
     * 通过WebSocket推送告警
     */
    private void pushAlertToWebSocket(AlertRecord alert) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ALERT");
            message.put("data", alert);
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/alerts", message);
            log.debug("告警已推送到WebSocket: {}", alert.getAlertName());
        } catch (Exception e) {
            log.warn("WebSocket推送告警失败: {}", e.getMessage());
        }
    }

    /**
     * 根据告警严重程度进行处理
     */
    private void handleAlertBySeverity(AlertRecord alert) {
        switch (alert.getSeverity()) {
            case "critical":
                log.error("【紧急告警】{}: {} - {}", alert.getAlertName(), alert.getSummary(), alert.getDescription());
                // 可以添加短信、电话通知等
                break;
            case "warning":
                log.warn("【警告告警】{}: {} - {}", alert.getAlertName(), alert.getSummary(), alert.getDescription());
                // 可以添加邮件通知等
                break;
            default:
                log.info("【信息告警】{}: {} - {}", alert.getAlertName(), alert.getSummary(), alert.getDescription());
        }
    }

    // ==================== 数据模型 ====================

    @Data
    public static class AlertmanagerPayload {
        private String receiver;
        private String status;
        private List<Alert> alerts;
        private Map<String, String> groupLabels;
        private Map<String, String> commonLabels;
        private Map<String, String> commonAnnotations;
        private String externalURL;
        private String version;
        private String groupKey;
    }

    @Data
    public static class Alert {
        private String status;
        private Map<String, String> labels;
        private Map<String, String> annotations;
        private String startsAt;
        private String endsAt;
        private String generatorURL;
        private String fingerprint;
    }

    @Data
    public static class AlertRecord {
        private String alertName;
        private String severity;
        private String instance;
        private String job;
        private String status;
        private String summary;
        private String description;
        private String startsAt;
        private String endsAt;
        private String receivedAt;
    }
}
