package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.entity.Warehouse;
import com.lorries.mobile.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库控制器
 */
@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "仓库管理", description = "仓库信息查询接口")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/{warehouseId}")
    @Operation(summary = "获取仓库详情", description = "根据ID获取仓库详细信息")
    public Result<Warehouse> getWarehouseDetail(@PathVariable Long warehouseId) {
        Warehouse warehouse = warehouseService.getWarehouseDetail(warehouseId);
        return Result.success(warehouse);
    }

    @GetMapping
    @Operation(summary = "获取仓库列表", description = "获取仓库列表")
    public Result<PageResult<Warehouse>> getWarehouseList(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Warehouse> result = warehouseService.getWarehouseList(status, keyword, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/active")
    @Operation(summary = "获取活跃仓库", description = "获取所有活跃状态的仓库")
    public Result<List<Warehouse>> getActiveWarehouses() {
        List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
        return Result.success(warehouses);
    }

    @GetMapping("/nearby")
    @Operation(summary = "获取附近仓库", description = "根据坐标获取附近的仓库")
    public Result<List<Warehouse>> getNearbyWarehouses(
            @Parameter(description = "经度") @RequestParam Double longitude,
            @Parameter(description = "纬度") @RequestParam Double latitude,
            @Parameter(description = "半径(公里)") @RequestParam(defaultValue = "10") Double radiusKm) {
        List<Warehouse> warehouses = warehouseService.getNearbyWarehouses(longitude, latitude, radiusKm);
        return Result.success(warehouses);
    }

    @GetMapping("/{warehouseId}/capacity")
    @Operation(summary = "获取仓库可用容量", description = "获取仓库的可用容量")
    public Result<Integer> getAvailableCapacity(@PathVariable Long warehouseId) {
        Integer capacity = warehouseService.getAvailableCapacity(warehouseId);
        return Result.success(capacity);
    }

    @PostMapping("/{warehouseId}/capacity")
    @Operation(summary = "更新仓库容量", description = "更新仓库使用容量")
    public Result<Void> updateCapacity(
            @PathVariable Long warehouseId,
            @Parameter(description = "使用容量") @RequestParam Integer usedCapacity) {
        warehouseService.updateCapacity(warehouseId, usedCapacity);
        return Result.success();
    }
}
