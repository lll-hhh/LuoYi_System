package com.lorries.hub.controller;

import com.lorries.hub.common.result.Result;
import com.lorries.hub.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计分析控制器
 */
@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final TrafficFlowService trafficFlowService;
    private final AnomalyService anomalyService;
    private final TaskService taskService;
    private final EmployeeService employeeService;
    private final WarehouseService warehouseService;
    private final ParkingService parkingService;

    @Operation(summary = "获取仪表盘概览数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // 今日数据
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // 车流量统计
        Map<String, Object> trafficStats = trafficFlowService.getStatistics(today, today);
        dashboard.put("traffic", trafficStats);
        
        // 异常统计
        Map<String, Object> anomalyStats = anomalyService.getStatistics(today, today);
        dashboard.put("anomaly", anomalyStats);
        
        // 任务统计
        Map<String, Object> taskStats = taskService.getStatistics(null);
        dashboard.put("task", taskStats);
        
        // 仓库概览
        Map<String, Object> warehouseOverview = warehouseService.getOverview();
        dashboard.put("warehouse", warehouseOverview);
        
        // 停车场概览
        Map<String, Object> parkingOverview = parkingService.getOverview();
        dashboard.put("parking", parkingOverview);
        
        // 员工统计
        Map<String, Object> employeeStats = employeeService.getStatistics();
        dashboard.put("employee", employeeStats);
        
        dashboard.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return Result.success(dashboard);
    }

    @Operation(summary = "获取车流量趋势")
    @GetMapping("/traffic/trend")
    public Result<List<Map<String, Object>>> getTrafficTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer roadId) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return Result.success(trafficFlowService.getHistory(roadId, startDate, endDate, "day"));
    }

    @Operation(summary = "获取异常类型分布")
    @GetMapping("/anomaly/distribution")
    public Result<List<Map<String, Object>>> getAnomalyDistribution() {
        return Result.success(anomalyService.statisticsByType());
    }

    @Operation(summary = "获取任务完成率")
    @GetMapping("/task/completion")
    public Result<Map<String, Object>> getTaskCompletion() {
        List<Map<String, Object>> statusStats = taskService.statisticsByStatus();
        
        long total = 0;
        long completed = 0;
        for (Map<String, Object> stat : statusStats) {
            long count = ((Number) stat.get("count")).longValue();
            total += count;
            if ("completed".equals(stat.get("status"))) {
                completed = count;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("completionRate", total > 0 ? (double) completed / total * 100 : 0);
        result.put("byStatus", statusStats);
        
        return Result.success(result);
    }

    @Operation(summary = "获取实时监控摘要")
    @GetMapping("/realtime/summary")
    public Result<Map<String, Object>> getRealtimeSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // 模拟实时数据
        summary.put("onlineCameras", 45);
        summary.put("totalCameras", 50);
        summary.put("currentVehicles", new Random().nextInt(100) + 200);
        summary.put("avgSpeed", 35 + new Random().nextInt(15));
        summary.put("pendingAnomalies", anomalyService.getUnhandledCount());
        summary.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return Result.success(summary);
    }

    @Operation(summary = "获取系统运行报告")
    @GetMapping("/report")
    public Result<Map<String, Object>> getSystemReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        Map<String, Object> report = new HashMap<>();
        Map<String, String> period = new HashMap<>();
        period.put("start", startDate);
        period.put("end", endDate);
        report.put("period", period);
        report.put("traffic", trafficFlowService.getStatistics(startDate, endDate));
        report.put("anomaly", anomalyService.getStatistics(startDate, endDate));
        report.put("warehouse", warehouseService.getOverview());
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return Result.success(report);
    }

    @Operation(summary = "获取热门道路排行")
    @GetMapping("/roads/ranking")
    public Result<List<Map<String, Object>>> getRoadsRanking() {
        return Result.success(trafficFlowService.getPeakHours(null));
    }
}
