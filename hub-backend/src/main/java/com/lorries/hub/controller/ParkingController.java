package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.ParkingLot;
import com.lorries.hub.entity.ParkingRecord;
import com.lorries.hub.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 停车场管理控制器
 */
@Tag(name = "停车场管理")
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @Operation(summary = "获取停车场列表")
    @GetMapping("/lots")
    public Result<List<ParkingLot>> listLots() {
        return Result.success(parkingService.listLots());
    }

    @Operation(summary = "获取停车场详情")
    @GetMapping("/lots/{id}")
    public Result<ParkingLot> getLotById(@PathVariable Integer id) {
        return Result.success(parkingService.getLotById(id));
    }

    @Operation(summary = "新增停车场")
    @PostMapping("/lots")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> saveLot(@RequestBody ParkingLot lot) {
        parkingService.saveLot(lot);
        return Result.success();
    }

    @Operation(summary = "更新停车场信息")
    @PutMapping("/lots/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> updateLot(@PathVariable Integer id, @RequestBody ParkingLot lot) {
        lot.setParkingLotId(id);
        parkingService.updateLot(lot);
        return Result.success();
    }

    @Operation(summary = "删除停车场")
    @DeleteMapping("/lots/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteLot(@PathVariable Integer id) {
        parkingService.removeLot(id);
        return Result.success();
    }

    @Operation(summary = "获取停车场实时状态")
    @GetMapping("/lots/{id}/status")
    public Result<Map<String, Object>> getLotStatus(@PathVariable Integer id) {
        return Result.success(parkingService.getLotStatus(id));
    }

    @Operation(summary = "获取所有停车场概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(parkingService.getOverview());
    }

    // ============ 停车记录 ============

    @Operation(summary = "分页查询停车记录")
    @GetMapping("/records")
    public Result<PageResult<ParkingRecord>> listRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer lotId,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) String status) {
        return Result.success(parkingService.findRecordPage(page, size, lotId, plateNumber, status));
    }

    @Operation(summary = "车辆入场")
    @PostMapping("/entry")
    public Result<ParkingRecord> vehicleEntry(@RequestBody Map<String, Object> entryInfo) {
        return Result.success(parkingService.vehicleEntry(entryInfo));
    }

    @Operation(summary = "车辆出场")
    @PostMapping("/exit")
    public Result<Map<String, Object>> vehicleExit(@RequestBody Map<String, Object> exitInfo) {
        return Result.success(parkingService.vehicleExit(exitInfo));
    }

    @Operation(summary = "查询车辆停车状态")
    @GetMapping("/vehicle/{plateNumber}")
    public Result<ParkingRecord> getVehicleStatus(@PathVariable String plateNumber) {
        return Result.success(parkingService.getVehicleStatus(plateNumber));
    }

    @Operation(summary = "获取停车场收入统计")
    @GetMapping("/revenue")
    public Result<Map<String, Object>> getRevenue(
            @RequestParam(required = false) Integer lotId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(parkingService.getRevenue(lotId, startDate, endDate));
    }

    @Operation(summary = "获取停车高峰时段分析")
    @GetMapping("/peak-hours")
    public Result<List<Map<String, Object>>> getPeakHours(@RequestParam(required = false) Integer lotId) {
        return Result.success(parkingService.getPeakHours(lotId));
    }
}
