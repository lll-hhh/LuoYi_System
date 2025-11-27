package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.dto.LocationReportRequest;
import com.lorries.mobile.entity.LocationRecord;
import com.lorries.mobile.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 位置控制器
 */
@RestController
@RequestMapping("/api/location")
@Tag(name = "位置管理", description = "位置上报和轨迹查询接口")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/report")
    @Operation(summary = "上报位置", description = "司机上报当前位置")
    public Result<Void> reportLocation(@Valid @RequestBody LocationReportRequest request,
                                        HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        Long vehicleId = (Long) httpRequest.getAttribute("vehicleId");
        locationService.reportLocation(driverId, vehicleId, request);
        return Result.success();
    }

    @PostMapping("/report/batch")
    @Operation(summary = "批量上报位置", description = "批量上报位置数据（用于离线同步）")
    public Result<Void> batchReportLocation(@Valid @RequestBody List<LocationReportRequest> requests,
                                             HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        Long vehicleId = (Long) httpRequest.getAttribute("vehicleId");
        locationService.batchReportLocation(driverId, vehicleId, requests);
        return Result.success();
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    @Operation(summary = "获取车辆最新位置", description = "获取指定车辆的最新位置")
    public Result<LocationRecord> getVehicleLatestLocation(@PathVariable Long vehicleId) {
        LocationRecord record = locationService.getLatestLocation(vehicleId);
        return Result.success(record);
    }

    @GetMapping("/driver/{driverId}/latest")
    @Operation(summary = "获取司机最新位置", description = "获取指定司机的最新位置")
    public Result<LocationRecord> getDriverLatestLocation(@PathVariable Long driverId) {
        LocationRecord record = locationService.getDriverLatestLocation(driverId);
        return Result.success(record);
    }

    @GetMapping("/vehicle/{vehicleId}/track")
    @Operation(summary = "获取车辆轨迹", description = "获取指定车辆在时间段内的轨迹")
    public Result<List<LocationRecord>> getVehicleTrack(
            @PathVariable Long vehicleId,
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<LocationRecord> records = locationService.getTrack(vehicleId, startTime, endTime);
        return Result.success(records);
    }

    @GetMapping("/task/{taskId}/track")
    @Operation(summary = "获取任务轨迹", description = "获取指定任务的轨迹")
    public Result<List<LocationRecord>> getTaskTrack(@PathVariable Long taskId) {
        List<LocationRecord> records = locationService.getTaskTrack(taskId);
        return Result.success(records);
    }

    @GetMapping("/vehicle/{vehicleId}/mileage")
    @Operation(summary = "计算行驶里程", description = "计算车辆在时间段内的行驶里程")
    public Result<Double> calculateMileage(
            @PathVariable Long vehicleId,
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Double mileage = locationService.calculateMileage(vehicleId, startTime, endTime);
        return Result.success(mileage);
    }
}
