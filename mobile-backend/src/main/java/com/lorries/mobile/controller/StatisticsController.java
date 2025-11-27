package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.dto.DashboardStats;
import com.lorries.mobile.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "数据统计", description = "数据统计和仪表盘接口")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘数据", description = "获取系统整体统计数据")
    public Result<DashboardStats> getDashboardStats() {
        DashboardStats stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }

    @GetMapping("/dashboard/driver")
    @Operation(summary = "获取司机仪表盘数据", description = "获取当前司机的统计数据")
    public Result<DashboardStats> getDriverDashboardStats(HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        DashboardStats stats = statisticsService.getDriverDashboardStats(driverId);
        return Result.success(stats);
    }

    @GetMapping("/tasks/today")
    @Operation(summary = "获取今日任务数", description = "获取今日任务总数")
    public Result<Integer> getTodayTaskCount() {
        Integer count = statisticsService.getTodayTaskCount();
        return Result.success(count);
    }

    @GetMapping("/mileage/today")
    @Operation(summary = "获取今日里程", description = "获取当前司机今日行驶里程")
    public Result<Double> getTodayMileage(HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        Double mileage = statisticsService.getTodayMileage(driverId);
        return Result.success(mileage);
    }

    @GetMapping("/mileage/month")
    @Operation(summary = "获取本月里程", description = "获取当前司机本月行驶里程")
    public Result<Double> getMonthMileage(HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        Double mileage = statisticsService.getMonthMileage(driverId);
        return Result.success(mileage);
    }

    @GetMapping("/vehicles/online")
    @Operation(summary = "获取在线车辆数", description = "获取当前在线车辆数量")
    public Result<Integer> getOnlineVehicleCount() {
        Integer count = statisticsService.getOnlineVehicleCount();
        return Result.success(count);
    }

    @GetMapping("/drivers/online")
    @Operation(summary = "获取在线司机数", description = "获取当前在线司机数量")
    public Result<Integer> getOnlineDriverCount() {
        Integer count = statisticsService.getOnlineDriverCount();
        return Result.success(count);
    }
}
