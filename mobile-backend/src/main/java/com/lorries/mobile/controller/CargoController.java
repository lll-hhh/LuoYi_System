package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.dto.CargoVO;
import com.lorries.mobile.service.CargoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 货物控制器
 */
@RestController
@RequestMapping("/api/cargos")
@Tag(name = "货物管理", description = "货物查询和签收接口")
public class CargoController {

    @Autowired
    private CargoService cargoService;

    @GetMapping("/tracking/{trackingNo}")
    @Operation(summary = "追踪货物", description = "根据追踪号查询货物信息")
    public Result<CargoVO> trackCargo(@PathVariable String trackingNo) {
        CargoVO cargo = cargoService.getByTrackingNo(trackingNo);
        return Result.success(cargo);
    }

    @GetMapping("/{cargoId}")
    @Operation(summary = "获取货物详情", description = "根据ID获取货物详细信息")
    public Result<CargoVO> getCargoDetail(@PathVariable Long cargoId) {
        CargoVO cargo = cargoService.getCargoDetail(cargoId);
        return Result.success(cargo);
    }

    @GetMapping
    @Operation(summary = "获取货物列表", description = "获取货物列表")
    public Result<PageResult<CargoVO>> getCargoList(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "货物类型") @RequestParam(required = false) String cargoType,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<CargoVO> result = cargoService.getCargoList(status, cargoType, keyword, page, pageSize);
        return Result.success(result);
    }

    @PostMapping("/{cargoId}/status")
    @Operation(summary = "更新货物状态", description = "更新货物状态")
    public Result<Void> updateStatus(
            @PathVariable Long cargoId,
            @Parameter(description = "状态") @RequestParam String status,
            @Parameter(description = "当前位置") @RequestParam(required = false) String currentLocation) {
        cargoService.updateStatus(cargoId, status, currentLocation);
        return Result.success();
    }

    @PostMapping("/{cargoId}/sign")
    @Operation(summary = "签收货物", description = "签收货物")
    public Result<Void> signCargo(
            @PathVariable Long cargoId,
            @Parameter(description = "签收人") @RequestParam String signedBy,
            @Parameter(description = "签名图片") @RequestParam(required = false) String signatureImage) {
        cargoService.signCargo(cargoId, signedBy, signatureImage);
        return Result.success();
    }

    @PostMapping("/inbound")
    @Operation(summary = "入库扫描", description = "货物入库扫描")
    public Result<Void> scanInbound(
            @Parameter(description = "追踪号") @RequestParam String trackingNo,
            @Parameter(description = "仓库ID") @RequestParam Long warehouseId,
            @Parameter(description = "库位") @RequestParam(required = false) String location) {
        cargoService.scanInbound(trackingNo, warehouseId, location);
        return Result.success();
    }

    @PostMapping("/outbound")
    @Operation(summary = "出库扫描", description = "货物出库扫描")
    public Result<Void> scanOutbound(
            @Parameter(description = "追踪号") @RequestParam String trackingNo,
            @Parameter(description = "任务ID") @RequestParam(required = false) Long taskId) {
        cargoService.scanOutbound(trackingNo, taskId);
        return Result.success();
    }
}
