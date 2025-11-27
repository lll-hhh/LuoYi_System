package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 员工Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("""
            SELECT e.*, d.department_name, r.role_name, r.role_code
            FROM employee e
            LEFT JOIN department d ON e.department_id = d.department_id
            LEFT JOIN role r ON e.role_id = r.role_id
            WHERE e.username = #{username}
            """)
    Employee findByUsername(@Param("username") String username);

    @Select("""
            SELECT e.*, d.department_name, r.role_name, r.role_code
            FROM employee e
            LEFT JOIN department d ON e.department_id = d.department_id
            LEFT JOIN role r ON e.role_id = r.role_id
            WHERE e.employee_id = #{employeeId}
            """)
    Employee findById(@Param("employeeId") Integer employeeId);
}
