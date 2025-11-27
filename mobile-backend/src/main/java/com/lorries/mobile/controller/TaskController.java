package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.dto.TaskCreateRequest;
import com.lorries.mobile.dto.TaskVO;
import com.lorries.mobile.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理", description = "运输任务相关接口")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    @Operation(summary = "创建任务", description = "创建新的运输任务")
    public Result<TaskVO> createTask(@Valid @RequestBody TaskCreateRequest request,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        TaskVO task = taskService.createTask(request, userId);
        return Result.success(task);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "根据任务ID获取任务详细信息")
    public Result<TaskVO> getTaskDetail(@PathVariable Long taskId) {
        TaskVO task = taskService.getTaskDetail(taskId);
        return Result.success(task);
    }

    @GetMapping("/driver")
    @Operation(summary = "获取司机任务列表", description = "获取当前司机的任务列表")
    public Result<PageResult<TaskVO>> getDriverTasks(
            HttpServletRequest httpRequest,
            @Parameter(description = "任务状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        PageResult<TaskVO> result = taskService.getDriverTasks(driverId, status, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前任务", description = "获取司机当前正在执行的任务")
    public Result<TaskVO> getCurrentTask(HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        TaskVO task = taskService.getCurrentTask(driverId);
        return Result.success(task);
    }

    @PostMapping("/{taskId}/assign")
    @Operation(summary = "分配任务", description = "将任务分配给指定司机和车辆")
    public Result<Void> assignTask(
            @PathVariable Long taskId,
            @Parameter(description = "司机ID") @RequestParam Long driverId,
            @Parameter(description = "车辆ID") @RequestParam Long vehicleId) {
        taskService.assignTask(taskId, driverId, vehicleId);
        return Result.success();
    }

    @PostMapping("/{taskId}/start")
    @Operation(summary = "开始任务", description = "司机开始执行任务")
    public Result<Void> startTask(@PathVariable Long taskId, HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        taskService.startTask(taskId, driverId);
        return Result.success();
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "完成任务", description = "司机完成任务")
    public Result<Void> completeTask(
            @PathVariable Long taskId,
            @Parameter(description = "实际行驶距离(公里)") @RequestParam Double actualDistance,
            HttpServletRequest httpRequest) {
        Long driverId = (Long) httpRequest.getAttribute("driverId");
        taskService.completeTask(taskId, driverId, actualDistance);
        return Result.success();
    }

    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消任务")
    public Result<Void> cancelTask(
            @PathVariable Long taskId,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        taskService.cancelTask(taskId, reason);
        return Result.success();
    }
}
