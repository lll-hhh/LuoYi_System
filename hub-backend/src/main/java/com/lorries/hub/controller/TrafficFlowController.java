package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.TrafficFlow;
import com.lorries.hub.service.TrafficFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 车流数据控制器
 */
@Tag(name = "车流数据管理")
@RestController
@RequestMapping("/api/traffic-flow")
@RequiredArgsConstructor
public class TrafficFlowController {

    private final TrafficFlowService trafficFlowService;

    @Operation(summary = "分页查询车流记录")
    @GetMapping
    public Result<PageResult<TrafficFlow>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer roadId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(trafficFlowService.findPage(page, size, roadId, startTime, endTime));
    }

    @Operation(summary = "获取实时车流数据")
    @GetMapping("/realtime")
    public Result<List<TrafficFlow>> getRealtimeFlow(@RequestParam(required = false) Integer roadId) {
        return Result.success(trafficFlowService.getRealtime(roadId));
    }

    @Operation(summary = "获取车流统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(trafficFlowService.getStatistics(startDate, endDate));
    }

    @Operation(summary = "获取历史趋势")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(
            @RequestParam(required = false) Integer roadId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String granularity) {
        return Result.success(trafficFlowService.getHistory(roadId, startDate, endDate, granularity));
    }

    @Operation(summary = "获取车流预测")
    @GetMapping("/predict")
    public Result<List<Map<String, Object>>> predict(
            @RequestParam Integer roadId,
            @RequestParam(defaultValue = "24") Integer hours) {
        return Result.success(trafficFlowService.predict(roadId, hours));
    }

    @Operation(summary = "获取高峰时段")
    @GetMapping("/peak-hours")
    public Result<List<Map<String, Object>>> getPeakHours(
            @RequestParam(required = false) Integer roadId) {
        return Result.success(trafficFlowService.getPeakHours(roadId));
    }

    @Operation(summary = "获取车流热力图数据")
    @GetMapping("/heatmap")
    public Result<List<Map<String, Object>>> getHeatmap(
            @RequestParam(required = false) String time) {
        return Result.success(trafficFlowService.getHeatmap(time));
    }

    @Operation(summary = "按车辆类型统计")
    @GetMapping("/vehicle-types")
    public Result<List<Map<String, Object>>> statisticsByVehicleType(
            @RequestParam Integer roadId,
            @RequestParam String date) {
        return Result.success(trafficFlowService.statisticsByVehicleType(roadId, date));
    }

    @Operation(summary = "获取拥堵分析")
    @GetMapping("/congestion/{roadId}")
    public Result<Map<String, Object>> getCongestionAnalysis(@PathVariable Integer roadId) {
        return Result.success(trafficFlowService.getCongestionAnalysis(roadId));
    }
}
