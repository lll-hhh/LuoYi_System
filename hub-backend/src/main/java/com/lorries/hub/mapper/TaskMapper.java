package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("""
            SELECT t.*, tt.type_name as task_type_name, 
                   a.real_name as assignee_name, c.real_name as creator_name
            FROM task t
            LEFT JOIN task_type tt ON t.task_type_id = tt.task_type_id
            LEFT JOIN employee a ON t.assignee_id = a.employee_id
            LEFT JOIN employee c ON t.created_by = c.employee_id
            WHERE t.task_id = #{taskId}
            """)
    Task findById(@Param("taskId") Integer taskId);
}
