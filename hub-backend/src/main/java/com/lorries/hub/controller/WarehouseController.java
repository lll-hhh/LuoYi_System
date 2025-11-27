package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.Warehouse;
import com.lorries.hub.entity.Cargo;
import com.lorries.hub.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物流仓储管理控制器
 */
@Tag(name = "物流仓储管理")
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "获取仓库列表")
    @GetMapping
    public Result<List<Warehouse>> list() {
        return Result.success(warehouseService.listWarehouses());
    }

    @Operation(summary = "获取仓库详情")
    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Integer id) {
        return Result.success(warehouseService.getById(id));
    }

    @Operation(summary = "新增仓库")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> save(@RequestBody Warehouse warehouse) {
        warehouseService.saveWarehouse(warehouse);
        return Result.success();
    }

    @Operation(summary = "更新仓库信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Warehouse warehouse) {
        warehouse.setWarehouseId(id);
        warehouseService.updateWarehouse(warehouse);
        return Result.success();
    }

    @Operation(summary = "删除仓库")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Integer id) {
        warehouseService.removeWarehouse(id);
        return Result.success();
    }

    @Operation(summary = "获取仓库统计")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getWarehouseStatistics(@PathVariable Integer id) {
        return Result.success(warehouseService.getWarehouseStatistics(id));
    }

    @Operation(summary = "获取仓库概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(warehouseService.getOverview());
    }

    // ============ 货物管理 ============

    @Operation(summary = "分页查询货物列表")
    @GetMapping("/cargo")
    public Result<PageResult<Cargo>> listCargo(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer warehouseId,
            @RequestParam(required = false) String cargoType,
            @RequestParam(required = false) String status) {
        return Result.success(warehouseService.findCargoPage(page, size, warehouseId, cargoType, status));
    }

    @Operation(summary = "获取货物详情")
    @GetMapping("/cargo/{id}")
    public Result<Cargo> getCargoById(@PathVariable Long id) {
        return Result.success(warehouseService.getCargoById(id));
    }

    @Operation(summary = "货物入库")
    @PostMapping("/cargo/inbound")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR')")
    public Result<Void> cargoInbound(@RequestBody Cargo cargo) {
        warehouseService.cargoInbound(cargo);
        return Result.success();
    }

    @Operation(summary = "货物出库")
    @PostMapping("/cargo/{id}/outbound")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR')")
    public Result<Void> cargoOutbound(@PathVariable Long id, @RequestBody Map<String, Object> outboundInfo) {
        warehouseService.cargoOutbound(id, outboundInfo);
        return Result.success();
    }

    @Operation(summary = "获取库存统计")
    @GetMapping("/inventory")
    public Result<Map<String, Object>> getInventoryStatistics(
            @RequestParam(required = false) Integer warehouseId) {
        return Result.success(warehouseService.getInventoryStatistics(warehouseId));
    }

    @Operation(summary = "按类型统计货物")
    @GetMapping("/cargo/statistics/type")
    public Result<List<Map<String, Object>>> statisticsByType(
            @RequestParam(required = false) Integer warehouseId) {
        return Result.success(warehouseService.statisticsByType(warehouseId));
    }
}
