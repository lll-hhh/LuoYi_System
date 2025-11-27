package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.entity.Vehicle;
import com.lorries.mobile.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆管理控制器
 */
@Tag(name = "车辆管理", description = "用户车辆的增删改查")
@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "获取我的车辆列表")
    @GetMapping("/my")
    public Result<List<Vehicle>> getMyVehicles() {
        return Result.success(vehicleService.getMyVehicles());
    }

    @Operation(summary = "添加车辆")
    @PostMapping
    public Result<Boolean> addVehicle(@RequestBody Vehicle vehicle) {
        return Result.success(vehicleService.addVehicle(vehicle));
    }

    @Operation(summary = "更新车辆信息")
    @PutMapping("/{id}")
    public Result<Boolean> updateVehicle(@PathVariable("id") Integer vehicleId, @RequestBody Vehicle vehicle) {
        vehicle.setVehicleId(vehicleId);
        return Result.success(vehicleService.updateVehicle(vehicle));
    }

    @Operation(summary = "删除车辆")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteVehicle(@PathVariable("id") Integer vehicleId) {
        return Result.success(vehicleService.deleteVehicle(vehicleId));
    }

    @Operation(summary = "设为默认车辆")
    @PostMapping("/{id}/default")
    public Result<Void> setDefaultVehicle(@PathVariable("id") Integer vehicleId) {
        vehicleService.setDefaultVehicle(vehicleId);
        return Result.success();
    }
}
