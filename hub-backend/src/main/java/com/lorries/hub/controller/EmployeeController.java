package com.lorries.hub.controller;

import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.common.result.Result;
import com.lorries.hub.entity.Employee;
import com.lorries.hub.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 员工管理控制器
 */
@Tag(name = "员工管理")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<PageResult<Employee>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position) {
        return Result.success(employeeService.findPage(page, size, name, department, position));
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Employee> getById(@PathVariable Integer id) {
        return Result.success(employeeService.getById(id));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> save(@RequestBody Employee employee) {
        employeeService.saveEmployee(employee);
        return Result.success();
    }

    @Operation(summary = "更新员工信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Employee employee) {
        employee.setEmployeeId(id);
        employeeService.updateEmployee(employee);
        return Result.success();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Integer id) {
        employeeService.removeEmployee(id);
        return Result.success();
    }

    @Operation(summary = "重置员工密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Integer id) {
        employeeService.resetPassword(id);
        return Result.success();
    }

    @Operation(summary = "更新员工状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        employeeService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "批量导入员工")
    @PostMapping("/batch-import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchImport(@RequestBody List<Employee> employees) {
        employeeService.batchImport(employees);
        return Result.success();
    }

    @Operation(summary = "导出员工列表")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<List<Employee>> exportEmployees(@RequestParam(required = false) String department) {
        return Result.success(employeeService.exportEmployees(department));
    }

    @Operation(summary = "获取员工统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(employeeService.getStatistics());
    }
}
