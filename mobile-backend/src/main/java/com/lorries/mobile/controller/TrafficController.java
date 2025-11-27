package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.entity.TrafficInfo;
import com.lorries.mobile.service.TrafficService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 交通信息控制器
 */
@Tag(name = "交通信息")
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;

    @Operation(summary = "获取区域交通状况")
    @GetMapping("/area")
    public Result<Map<String, Object>> getAreaTraffic(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radius) {
        return Result.success(trafficService.getAreaTraffic(lat, lng, radius));
    }

    @Operation(summary = "获取道路交通状况")
    @GetMapping("/road/{roadId}")
    public Result<TrafficInfo> getRoadTraffic(@PathVariable Integer roadId) {
        return Result.success(trafficService.getRoadTraffic(roadId));
    }

    @Operation(summary = "获取交通事件列表")
    @GetMapping("/events")
    public Result<List<Map<String, Object>>> getTrafficEvents(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10") Double radius) {
        return Result.success(trafficService.getTrafficEvents(lat, lng, radius));
    }

    @Operation(summary = "上报交通事件")
    @PostMapping("/events")
    public Result<Void> reportTrafficEvent(@RequestBody Map<String, Object> eventInfo) {
        trafficService.reportTrafficEvent(eventInfo);
        return Result.success();
    }

    @Operation(summary = "获取交通预测")
    @GetMapping("/predict")
    public Result<List<Map<String, Object>>> getTrafficPrediction(
            @RequestParam Integer roadId,
            @RequestParam(defaultValue = "24") Integer hours) {
        return Result.success(trafficService.getTrafficPrediction(roadId, hours));
    }

    @Operation(summary = "获取热力图数据")
    @GetMapping("/heatmap")
    public Result<List<Map<String, Object>>> getTrafficHeatmap(
            @RequestParam Double minLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLat,
            @RequestParam Double maxLng) {
        return Result.success(trafficService.getTrafficHeatmap(minLat, minLng, maxLat, maxLng));
    }

    @Operation(summary = "获取拥堵排行")
    @GetMapping("/congestion/ranking")
    public Result<List<Map<String, Object>>> getCongestionRanking() {
        return Result.success(trafficService.getCongestionRanking());
    }

    @Operation(summary = "订阅路况推送")
    @PostMapping("/subscribe")
    public Result<Void> subscribeTrafficPush(@RequestBody Map<String, Object> subscribeInfo) {
        trafficService.subscribeTrafficPush(subscribeInfo);
        return Result.success();
    }

    @Operation(summary = "取消订阅路况推送")
    @DeleteMapping("/subscribe")
    public Result<Void> unsubscribeTrafficPush(@RequestParam Integer userId) {
        trafficService.unsubscribeTrafficPush(userId);
        return Result.success();
    }
}
