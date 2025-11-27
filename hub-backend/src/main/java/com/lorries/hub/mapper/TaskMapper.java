package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 查询任务（带关联信息）
     */
    @Select("SELECT t.*, tt.type_name as task_type_name, " +
            "       e1.real_name as assignee_name, e2.real_name as creator_name " +
            "FROM task t " +
            "LEFT JOIN task_type tt ON t.task_type_id = tt.task_type_id " +
            "LEFT JOIN employee e1 ON t.assignee_id = e1.employee_id " +
            "LEFT JOIN employee e2 ON t.created_by = e2.employee_id " +
            "WHERE t.task_id = #{id}")
    Task selectByIdWithRelations(@Param("id") Long id);

    /**
     * 获取用户的任务
     */
    @Select("SELECT t.*, tt.type_name as task_type_name " +
            "FROM task t " +
            "LEFT JOIN task_type tt ON t.task_type_id = tt.task_type_id " +
            "WHERE t.assignee_id = #{userId} " +
            "AND (#{status} IS NULL OR t.status = #{status}) " +
            "ORDER BY t.created_at DESC")
    List<Task> selectByAssignee(@Param("userId") Integer userId, @Param("status") String status);

    /**
     * 按状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM task GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 按优先级统计
     */
    @Select("SELECT priority, COUNT(*) as count FROM task GROUP BY priority")
    List<Map<String, Object>> countByPriority();

    /**
     * 获取用户任务统计
     */
    @Select("SELECT status, COUNT(*) as count FROM task " +
            "WHERE assignee_id = #{assigneeId} GROUP BY status")
    List<Map<String, Object>> countByStatusForUser(@Param("assigneeId") Integer assigneeId);
}
