package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.dto.DriverVO;
import com.lorries.mobile.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 司机控制器
 */
@RestController
@RequestMapping("/api/drivers")
@Tag(name = "司机管理", description = "司机信息和状态管理接口")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @GetMapping("/profile")
    @Operation(summary = "获取当前司机信息", description = "获取当前登录司机的详细信息")
    public Result<DriverVO> getProfile(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        DriverVO driver = driverService.getByUserId(userId);
        return Result.success(driver);
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "获取司机详情", description = "根据ID获取司机详细信息")
    public Result<DriverVO> getDriverDetail(@PathVariable Long driverId) {
        DriverVO driver = driverService.getDriverDetail(driverId);
        return Result.success(driver);
    }

    @GetMapping
    @Operation(summary = "获取司机列表", description = "获取司机列表")
    public Result<PageResult<DriverVO>> getDriverList(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<DriverVO> result = driverService.getDriverList(status, keyword, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用司机列表", description = "获取所有空闲状态的司机")
    public Result<List<DriverVO>> getAvailableDrivers() {
        List<DriverVO> drivers = driverService.getAvailableDrivers();
        return Result.success(drivers);
    }

    @PostMapping("/status")
    @Operation(summary = "更新司机状态", description = "更新当前司机的状态（上线/下线）")
    public Result<Void> updateStatus(
            @Parameter(description = "状态") @RequestParam String status,
            HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        driverService.updateStatus(driverId, status);
        return Result.success();
    }

    @PostMapping("/location")
    @Operation(summary = "更新司机位置", description = "更新司机当前位置")
    public Result<Void> updateLocation(
            @Parameter(description = "经度") @RequestParam Double longitude,
            @Parameter(description = "纬度") @RequestParam Double latitude,
            HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        driverService.updateLocation(driverId, longitude, latitude);
        return Result.success();
    }

    @PostMapping("/vehicle/bind")
    @Operation(summary = "绑定车辆", description = "司机绑定车辆")
    public Result<Void> bindVehicle(
            @Parameter(description = "车辆ID") @RequestParam Long vehicleId,
            HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        driverService.bindVehicle(driverId, vehicleId);
        return Result.success();
    }

    @PostMapping("/vehicle/unbind")
    @Operation(summary = "解绑车辆", description = "司机解绑车辆")
    public Result<Void> unbindVehicle(HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        driverService.unbindVehicle(driverId);
        return Result.success();
    }

    @GetMapping("/{driverId}/statistics")
    @Operation(summary = "获取司机统计", description = "获取司机的任务完成数和里程统计")
    public Result<Object> getDriverStatistics(@PathVariable Long driverId) {
        Integer completedTasks = driverService.getCompletedTaskCount(driverId);
        Double totalMileage = driverService.getTotalMileage(driverId);
        return Result.success(new Object() {
            public final Integer tasks = completedTasks;
            public final Double mileage = totalMileage;
        });
    }
}
