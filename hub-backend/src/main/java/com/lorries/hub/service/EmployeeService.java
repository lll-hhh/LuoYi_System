package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Employee;

import java.util.List;
import java.util.Map;

/**
 * 员工服务接口
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 分页查询员工
     */
    PageResult<Employee> findPage(Integer page, Integer size, String name, String department, String position);

    /**
     * 根据ID获取员工
     */
    Employee getById(Integer id);

    /**
     * 新增员工
     */
    void saveEmployee(Employee employee);

    /**
     * 更新员工信息
     */
    void updateEmployee(Employee employee);

    /**
     * 删除员工
     */
    void removeEmployee(Integer id);

    /**
     * 重置员工密码
     */
    void resetPassword(Integer id);

    /**
     * 更新员工状态
     */
    void updateStatus(Integer id, String status);

    /**
     * 批量导入员工
     */
    void batchImport(List<Employee> employees);

    /**
     * 导出员工列表
     */
    List<Employee> exportEmployees(String department);

    /**
     * 获取员工统计信息
     */
    Map<String, Object> getStatistics();
}
