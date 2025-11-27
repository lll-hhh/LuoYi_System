package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.entity.CargoDeclaration;
import com.lorries.mobile.service.CargoDeclarationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 货物申报控制器
 */
@Tag(name = "货物申报", description = "货物运输申报功能")
@RestController
@RequestMapping("/api/cargo")
@RequiredArgsConstructor
public class CargoDeclarationController {

    private final CargoDeclarationService cargoDeclarationService;

    @Operation(summary = "分页查询我的申报记录")
    @GetMapping("/my")
    public Result<PageResult<CargoDeclaration>> getMyDeclarations(
        HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cargoDeclarationService.getMyDeclarations(userId, page, size, status));
    }

    @Operation(summary = "获取申报详情")
    @GetMapping("/{id}")
    public Result<CargoDeclaration> getDeclaration(@PathVariable("id") Integer declarationId,
                                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cargoDeclarationService.getMyDeclaration(userId, declarationId));
    }

    @Operation(summary = "提交货物申报")
    @PostMapping
    public Result<CargoDeclaration> submitDeclaration(@RequestBody CargoDeclaration declaration,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cargoDeclarationService.submit(userId, declaration));
    }

    @Operation(summary = "取消申报")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelDeclaration(@PathVariable("id") Integer declarationId,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cargoDeclarationService.cancel(userId, declarationId);
        return Result.success();
    }

    @Operation(summary = "更新运输状态")
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Integer declarationId,
                                      @RequestParam String status) {
        cargoDeclarationService.updateStatus(declarationId, status);
        return Result.success();
    }
}
