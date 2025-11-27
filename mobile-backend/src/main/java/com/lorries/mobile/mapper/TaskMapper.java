package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.TransportTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<TransportTask> {
    
    /**
     * 统计各状态任务数
     */
    @Select("SELECT status, COUNT(*) as count FROM transport_task GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 获取司机今日任务统计
     */
    @Select("SELECT COUNT(*) FROM transport_task WHERE driver_id = #{driverId} " +
            "AND DATE(created_at) = CURRENT_DATE")
    Integer countDriverTodayTasks(@Param("driverId") Long driverId);
    
    /**
     * 获取时间段内的任务数
     */
    @Select("SELECT COUNT(*) FROM transport_task WHERE created_at BETWEEN #{startTime} AND #{endTime}")
    Integer countByTimeRange(@Param("startTime") LocalDateTime startTime, 
                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取司机完成任务数
     */
    @Select("SELECT COUNT(*) FROM transport_task WHERE driver_id = #{driverId} AND status = 'COMPLETED'")
    Integer countDriverCompletedTasks(@Param("driverId") Long driverId);
    
    /**
     * 获取任务趋势数据（按天统计）
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM transport_task " +
            "WHERE created_at >= #{startTime} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getTaskTrendByDay(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取按优先级统计
     */
    @Select("SELECT priority, COUNT(*) as count FROM transport_task GROUP BY priority")
    List<Map<String, Object>> countByPriority();
}
