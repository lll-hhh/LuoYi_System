package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 员工Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("SELECT * FROM employee WHERE username = #{username}")
    Employee findByUsername(@Param("username") String username);

    /**
     * 查询员工（带关联信息）
     */
    @Select("SELECT e.*, d.department_name, r.role_name, r.role_code " +
            "FROM employee e " +
            "LEFT JOIN department d ON e.department_id = d.department_id " +
            "LEFT JOIN role r ON e.role_id = r.role_id " +
            "WHERE e.employee_id = #{id}")
    Employee selectByIdWithRelations(@Param("id") Integer id);

    /**
     * 统计各部门员工数量
     */
    @Select("SELECT d.department_name, COUNT(e.employee_id) as count " +
            "FROM department d " +
            "LEFT JOIN employee e ON d.department_id = e.department_id " +
            "GROUP BY d.department_id, d.department_name")
    List<Map<String, Object>> countByDepartment();

    /**
     * 统计各状态员工数量
     */
    @Select("SELECT status, COUNT(*) as count FROM employee GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
