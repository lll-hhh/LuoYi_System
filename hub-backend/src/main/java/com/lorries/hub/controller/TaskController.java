package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.Task;
import com.lorries.hub.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "分页查询任务列表")
    @GetMapping
    public Result<PageResult<Task>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer assigneeId) {
        return Result.success(taskService.findPage(page, size, type, status, assigneeId));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @Operation(summary = "创建任务")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> save(@RequestBody Task task) {
        taskService.saveTask(task);
        return Result.success();
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Task task) {
        task.setTaskId(id.intValue());
        taskService.updateTask(task);
        return Result.success();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.removeTask(id);
        return Result.success();
    }

    @Operation(summary = "分配任务")
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> assignTask(@PathVariable Long id, @RequestParam Integer assigneeId) {
        taskService.assignTask(id, assigneeId);
        return Result.success();
    }

    @Operation(summary = "开始任务")
    @PostMapping("/{id}/start")
    public Result<Void> startTask(@PathVariable Long id) {
        taskService.startTask(id);
        return Result.success();
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> result) {
        taskService.completeTask(id, result);
        return Result.success();
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> cancelTask(@PathVariable Long id, @RequestParam String reason) {
        taskService.cancelTask(id, reason);
        return Result.success();
    }

    @Operation(summary = "获取我的任务列表")
    @GetMapping("/my")
    public Result<List<Task>> getMyTasks(
            @RequestParam Integer userId,
            @RequestParam(required = false) String status) {
        return Result.success(taskService.getMyTasks(userId, status));
    }

    @Operation(summary = "获取任务统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam(required = false) Integer assigneeId) {
        return Result.success(taskService.getStatistics(assigneeId));
    }

    @Operation(summary = "按状态统计任务")
    @GetMapping("/statistics/status")
    public Result<List<Map<String, Object>>> statisticsByStatus() {
        return Result.success(taskService.statisticsByStatus());
    }

    @Operation(summary = "批量分配任务")
    @PostMapping("/batch-assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> batchAssign(@RequestBody List<Long> taskIds, @RequestParam Integer assigneeId) {
        taskService.batchAssign(taskIds, assigneeId);
        return Result.success();
    }
}
