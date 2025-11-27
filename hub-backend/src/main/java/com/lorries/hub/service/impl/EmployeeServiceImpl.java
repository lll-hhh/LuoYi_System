package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Employee;
import com.lorries.hub.mapper.EmployeeMapper;
import com.lorries.hub.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工服务实现
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<Employee> findPage(Integer page, Integer size, String name, String department, String position) {
        Page<Employee> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Employee::getRealName, name)
                   .or().like(Employee::getUsername, name);
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(Employee::getDepartmentId, Integer.parseInt(department));
        }
        
        wrapper.orderByDesc(Employee::getCreatedAt);
        Page<Employee> result = page(pageParam, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public Employee getById(Integer id) {
        return baseMapper.selectByIdWithRelations(id);
    }

    @Override
    @Transactional
    public void saveEmployee(Employee employee) {
        // 加密密码
        if (StringUtils.hasText(employee.getPassword())) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        } else {
            // 默认密码
            employee.setPassword(passwordEncoder.encode("123456"));
        }
        employee.setStatus("active");
        employee.setLoginCount(0);
        save(employee);
    }

    @Override
    @Transactional
    public void updateEmployee(Employee employee) {
        // 不更新密码
        employee.setPassword(null);
        updateById(employee);
    }

    @Override
    @Transactional
    public void removeEmployee(Integer id) {
        removeById(id);
    }

    @Override
    @Transactional
    public void resetPassword(Integer id) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setPassword(passwordEncoder.encode("123456"));
        updateById(employee);
    }

    @Override
    @Transactional
    public void updateStatus(Integer id, String status) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setStatus(status);
        updateById(employee);
    }

    @Override
    @Transactional
    public void batchImport(List<Employee> employees) {
        for (Employee employee : employees) {
            if (!StringUtils.hasText(employee.getPassword())) {
                employee.setPassword(passwordEncoder.encode("123456"));
            } else {
                employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            }
            employee.setStatus("active");
            employee.setLoginCount(0);
        }
        saveBatch(employees);
    }

    @Override
    public List<Employee> exportEmployees(String department) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(department)) {
            wrapper.eq(Employee::getDepartmentId, Integer.parseInt(department));
        }
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", count());
        stats.put("byDepartment", baseMapper.countByDepartment());
        stats.put("byStatus", baseMapper.countByStatus());
        return stats;
    }
}
