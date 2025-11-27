package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.Anomaly;
import com.lorries.hub.service.AnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 异常信息处理控制器
 */
@Tag(name = "异常信息处理")
@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyService anomalyService;

    @Operation(summary = "分页查询异常记录")
    @GetMapping
    public Result<PageResult<Anomaly>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String level) {
        return Result.success(anomalyService.findPage(page, size, type, status, level));
    }

    @Operation(summary = "获取异常详情")
    @GetMapping("/{id}")
    public Result<Anomaly> getById(@PathVariable Long id) {
        return Result.success(anomalyService.getById(id));
    }

    @Operation(summary = "处理异常")
    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR')")
    public Result<Void> handleAnomaly(
            @PathVariable Long id,
            @RequestBody Map<String, Object> handleInfo) {
        anomalyService.handleAnomaly(id, handleInfo);
        return Result.success();
    }

    @Operation(summary = "关闭异常")
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> closeAnomaly(@PathVariable Long id, @RequestParam String remark) {
        anomalyService.closeAnomaly(id, remark);
        return Result.success();
    }

    @Operation(summary = "获取未处理异常数量")
    @GetMapping("/unhandled/count")
    public Result<Long> getUnhandledCount() {
        return Result.success(anomalyService.getUnhandledCount());
    }

    @Operation(summary = "获取异常统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(anomalyService.getStatistics(startDate, endDate));
    }

    @Operation(summary = "按类型统计异常")
    @GetMapping("/statistics/type")
    public Result<List<Map<String, Object>>> statisticsByType() {
        return Result.success(anomalyService.statisticsByType());
    }

    @Operation(summary = "获取最近异常")
    @GetMapping("/recent")
    public Result<List<Anomaly>> getRecentAnomalies(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(anomalyService.getRecentAnomalies(limit));
    }

    @Operation(summary = "批量处理异常")
    @PostMapping("/batch-handle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> batchHandle(@RequestBody List<Long> ids, @RequestBody Map<String, Object> handleInfo) {
        anomalyService.batchHandle(ids, handleInfo);
        return Result.success();
    }

    @Operation(summary = "批量关闭异常")
    @PostMapping("/batch-close")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> batchClose(@RequestBody List<Long> ids, @RequestParam String remark) {
        anomalyService.batchClose(ids, remark);
        return Result.success();
    }
}
