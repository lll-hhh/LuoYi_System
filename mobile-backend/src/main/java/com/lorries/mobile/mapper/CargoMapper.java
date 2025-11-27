package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.Cargo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 货物Mapper
 */
@Mapper
public interface CargoMapper extends BaseMapper<Cargo> {
    
    /**
     * 根据追踪号查询货物
     */
    @Select("SELECT * FROM cargo WHERE tracking_no = #{trackingNo}")
    Cargo getByTrackingNo(@Param("trackingNo") String trackingNo);
    
    /**
     * 统计各状态货物数
     */
    @Select("SELECT status, COUNT(*) as count FROM cargo GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计各类型货物数
     */
    @Select("SELECT cargo_type, COUNT(*) as count FROM cargo GROUP BY cargo_type")
    List<Map<String, Object>> countByType();
    
    /**
     * 获取今日签收数
     */
    @Select("SELECT COUNT(*) FROM cargo WHERE status = 'DELIVERED' AND DATE(signed_at) = CURRENT_DATE")
    Integer countTodayDelivered();
    
    /**
     * 获取待签收货物数
     */
    @Select("SELECT COUNT(*) FROM cargo WHERE status IN ('TRANSIT', 'ARRIVED')")
    Integer countPendingDelivery();
    
    /**
     * 获取仓库货物统计
     */
    @Select("SELECT warehouse_id, COUNT(*) as count FROM cargo " +
            "WHERE warehouse_id IS NOT NULL GROUP BY warehouse_id")
    List<Map<String, Object>> countByWarehouse();
    
    /**
     * 获取货物趋势（按天统计）
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM cargo " +
            "WHERE created_at >= #{startTime} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getTrendByDay(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取即将超时货物
     */
    @Select("SELECT * FROM cargo WHERE status = 'TRANSIT' " +
            "AND expected_arrival < #{deadline} ORDER BY expected_arrival")
    List<Cargo> getExpiringCargos(@Param("deadline") LocalDateTime deadline);
}
