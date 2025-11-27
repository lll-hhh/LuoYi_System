package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.dto.AnomalyReportRequest;
import com.lorries.mobile.dto.AnomalyVO;
import com.lorries.mobile.service.AnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 异常事件控制器
 */
@RestController
@RequestMapping("/api/anomalies")
@Tag(name = "异常管理", description = "异常事件上报和处理接口")
public class AnomalyController {

    @Autowired
    private AnomalyService anomalyService;

    @PostMapping
    @Operation(summary = "上报异常", description = "司机上报异常事件")
    public Result<AnomalyVO> reportAnomaly(@Valid @RequestBody AnomalyReportRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AnomalyVO anomaly = anomalyService.reportAnomaly(userId, request);
        return Result.success(anomaly);
    }

    @GetMapping("/{anomalyId}")
    @Operation(summary = "获取异常详情", description = "根据ID获取异常事件详情")
    public Result<AnomalyVO> getAnomalyDetail(@PathVariable Long anomalyId) {
        AnomalyVO anomaly = anomalyService.getAnomalyDetail(anomalyId);
        return Result.success(anomaly);
    }

    @GetMapping
    @Operation(summary = "获取异常列表", description = "获取异常事件列表")
    public Result<PageResult<AnomalyVO>> getAnomalyList(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "类型") @RequestParam(required = false) String eventType,
            @Parameter(description = "严重程度") @RequestParam(required = false) String severity,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<AnomalyVO> result = anomalyService.getAnomalyList(status, eventType, severity, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/driver")
    @Operation(summary = "获取司机异常列表", description = "获取当前司机上报的异常事件")
    public Result<PageResult<AnomalyVO>> getDriverAnomalies(
            HttpServletRequest httpRequest,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        PageResult<AnomalyVO> result = anomalyService.getDriverAnomalies(driverId, page, pageSize);
        return Result.success(result);
    }

    @PostMapping("/{anomalyId}/handle")
    @Operation(summary = "开始处理异常", description = "管理员开始处理异常事件")
    public Result<Void> handleAnomaly(@PathVariable Long anomalyId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        anomalyService.handleAnomaly(anomalyId, userId);
        return Result.success();
    }

    @PostMapping("/{anomalyId}/resolve")
    @Operation(summary = "解决异常", description = "标记异常事件已解决")
    public Result<Void> resolveAnomaly(
            @PathVariable Long anomalyId,
            @Parameter(description = "解决方案") @RequestParam String resolution,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        anomalyService.resolveAnomaly(anomalyId, userId, resolution);
        return Result.success();
    }

    @PostMapping("/{anomalyId}/close")
    @Operation(summary = "关闭异常", description = "关闭异常事件")
    public Result<Void> closeAnomaly(@PathVariable Long anomalyId) {
        anomalyService.closeAnomaly(anomalyId);
        return Result.success();
    }

    @GetMapping("/pending/count")
    @Operation(summary = "获取待处理异常数", description = "获取待处理异常事件数量")
    public Result<Integer> getPendingCount() {
        Integer count = anomalyService.getPendingCount();
        return Result.success(count);
    }
}
