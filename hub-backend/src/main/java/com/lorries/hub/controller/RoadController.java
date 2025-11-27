package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.Road;
import com.lorries.hub.service.RoadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 道路管理控制器
 */
@Tag(name = "道路管理", description = "道路的增删改查")
@RestController
@RequestMapping("/road")
@RequiredArgsConstructor
public class RoadController {

    private final RoadService roadService;

    @Operation(summary = "分页查询道路")
    @GetMapping("/page")
    public Result<PageResult<Road>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roadLevel,
            @RequestParam(required = false) String status) {
        return Result.success(roadService.findPage(page, size, roadLevel, status));
    }

    @Operation(summary = "获取道路详情")
    @GetMapping("/{id}")
    public Result<Road> getById(@PathVariable("id") Integer roadId) {
        return Result.success(roadService.findById(roadId));
    }

    @Operation(summary = "新增道路")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Boolean> save(@RequestBody Road road) {
        return Result.success(roadService.save(road));
    }

    @Operation(summary = "更新道路")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Result<Boolean> update(@PathVariable("id") Integer roadId, @RequestBody Road road) {
        road.setRoadId(roadId);
        return Result.success(roadService.updateById(road));
    }

    @Operation(summary = "删除道路")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable("id") Integer roadId) {
        return Result.success(roadService.removeById(roadId));
    }
}
