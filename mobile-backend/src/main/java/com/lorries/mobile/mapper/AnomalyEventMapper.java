package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.AnomalyEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常事件Mapper
 */
@Mapper
public interface AnomalyEventMapper extends BaseMapper<AnomalyEvent> {
    
    /**
     * 统计各状态异常数
     */
    @Select("SELECT status, COUNT(*) as count FROM anomaly_event GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计各类型异常数
     */
    @Select("SELECT event_type, COUNT(*) as count FROM anomaly_event GROUP BY event_type")
    List<Map<String, Object>> countByType();
    
    /**
     * 统计各严重程度异常数
     */
    @Select("SELECT severity, COUNT(*) as count FROM anomaly_event GROUP BY severity")
    List<Map<String, Object>> countBySeverity();
    
    /**
     * 获取待处理异常数
     */
    @Select("SELECT COUNT(*) FROM anomaly_event WHERE status = 'PENDING'")
    Integer countPending();
    
    /**
     * 获取今日异常数
     */
    @Select("SELECT COUNT(*) FROM anomaly_event WHERE DATE(created_at) = CURRENT_DATE")
    Integer countToday();
    
    /**
     * 获取异常趋势（按天统计）
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM anomaly_event " +
            "WHERE created_at >= #{startTime} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getTrendByDay(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取严重异常列表
     */
    @Select("SELECT * FROM anomaly_event WHERE severity = 'CRITICAL' AND status = 'PENDING' " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<AnomalyEvent> getCriticalPending(@Param("limit") Integer limit);
    
    /**
     * 获取司机异常统计
     */
    @Select("SELECT driver_id, COUNT(*) as count FROM anomaly_event " +
            "WHERE driver_id IS NOT NULL GROUP BY driver_id ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getDriverAnomalyRanking(@Param("limit") Integer limit);
}
