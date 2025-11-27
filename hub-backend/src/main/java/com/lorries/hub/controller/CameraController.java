package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.Camera;
import com.lorries.hub.service.CameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 摄像头管理控制器
 */
@Tag(name = "摄像头管理", description = "摄像头的增删改查和状态管理")
@RestController
@RequestMapping("/camera")
@RequiredArgsConstructor
public class CameraController {

    private final CameraService cameraService;

    @Operation(summary = "分页查询摄像头")
    @GetMapping("/page")
    public Result<PageResult<Camera>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer roadId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String onlineStatus) {
        return Result.success(cameraService.findPage(page, size, roadId, status, onlineStatus));
    }

    @Operation(summary = "获取摄像头详情")
    @GetMapping("/{id}")
    public Result<Camera> getById(@PathVariable("id") Integer cameraId) {
        return Result.success(cameraService.findById(cameraId));
    }

    @Operation(summary = "获取在线摄像头列表")
    @GetMapping("/online")
    public Result<List<Camera>> getOnlineCameras() {
        return Result.success(cameraService.findOnlineCameras());
    }

    @Operation(summary = "新增摄像头")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Boolean> save(@RequestBody Camera camera) {
        return Result.success(cameraService.save(camera));
    }

    @Operation(summary = "更新摄像头")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Boolean> update(@PathVariable("id") Integer cameraId, @RequestBody Camera camera) {
        camera.setCameraId(cameraId);
        return Result.success(cameraService.updateById(camera));
    }

    @Operation(summary = "删除摄像头")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable("id") Integer cameraId) {
        return Result.success(cameraService.removeById(cameraId));
    }

    @Operation(summary = "更新摄像头心跳")
    @PostMapping("/{id}/heartbeat")
    public Result<Void> heartbeat(@PathVariable("id") Integer cameraId) {
        cameraService.updateHeartbeat(cameraId);
        return Result.success();
    }
}
