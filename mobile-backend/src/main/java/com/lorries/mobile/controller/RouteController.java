package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.entity.Route;
import com.lorries.mobile.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 路线管理控制器
 */
@Tag(name = "路线管理")
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "获取推荐路线")
    @PostMapping("/recommend")
    public Result<List<Route>> getRecommendedRoutes(@RequestBody Map<String, Object> routeRequest) {
        return Result.success(routeService.getRecommendedRoutes(routeRequest));
    }

    @Operation(summary = "获取我的历史路线")
    @GetMapping("/history")
    public Result<List<Route>> getHistoryRoutes(@RequestParam Integer userId) {
        return Result.success(routeService.getHistoryRoutes(userId));
    }

    @Operation(summary = "获取路线详情")
    @GetMapping("/{id}")
    public Result<Route> getRouteById(@PathVariable Long id) {
        return Result.success(routeService.getById(id));
    }

    @Operation(summary = "保存路线")
    @PostMapping
    public Result<Void> saveRoute(@RequestBody Route route) {
        routeService.saveRoute(route);
        return Result.success();
    }

    @Operation(summary = "收藏路线")
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteRoute(@PathVariable Long id, @RequestParam Integer userId) {
        routeService.favoriteRoute(id, userId);
        return Result.success();
    }

    @Operation(summary = "取消收藏路线")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteRoute(@PathVariable Long id, @RequestParam Integer userId) {
        routeService.unfavoriteRoute(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取收藏的路线")
    @GetMapping("/favorites")
    public Result<List<Route>> getFavoriteRoutes(@RequestParam Integer userId) {
        return Result.success(routeService.getFavoriteRoutes(userId));
    }

    @Operation(summary = "开始导航")
    @PostMapping("/{id}/navigate")
    public Result<Map<String, Object>> startNavigation(@PathVariable Long id) {
        return Result.success(routeService.startNavigation(id));
    }

    @Operation(summary = "更新导航位置")
    @PostMapping("/{id}/location")
    public Result<Map<String, Object>> updateLocation(@PathVariable Long id, @RequestBody Map<String, Object> location) {
        return Result.success(routeService.updateLocation(id, location));
    }

    @Operation(summary = "结束导航")
    @PostMapping("/{id}/finish")
    public Result<Void> finishNavigation(@PathVariable Long id) {
        routeService.finishNavigation(id);
        return Result.success();
    }

    @Operation(summary = "获取实时路况")
    @GetMapping("/traffic")
    public Result<Map<String, Object>> getTrafficInfo(
            @RequestParam Double startLat,
            @RequestParam Double startLng,
            @RequestParam Double endLat,
            @RequestParam Double endLng) {
        return Result.success(routeService.getTrafficInfo(startLat, startLng, endLat, endLng));
    }
}
