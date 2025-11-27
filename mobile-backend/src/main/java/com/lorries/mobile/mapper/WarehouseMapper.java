package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 仓库Mapper
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
    
    /**
     * 获取活跃仓库列表
     */
    @Select("SELECT * FROM warehouse WHERE status = 'ACTIVE' ORDER BY name")
    List<Warehouse> getActiveWarehouses();
    
    /**
     * 统计各状态仓库数
     */
    @Select("SELECT status, COUNT(*) as count FROM warehouse GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 获取仓库容量统计
     */
    @Select("SELECT id, name, total_capacity, used_capacity, " +
            "(total_capacity - used_capacity) as available_capacity, " +
            "ROUND(used_capacity * 100.0 / total_capacity, 2) as usage_rate " +
            "FROM warehouse WHERE status = 'ACTIVE'")
    List<Map<String, Object>> getCapacityStats();
    
    /**
     * 获取附近仓库
     */
    @Select("SELECT *, " +
            "6371 * acos(cos(radians(#{latitude})) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(#{longitude})) + " +
            "sin(radians(#{latitude})) * sin(radians(latitude))) as distance " +
            "FROM warehouse WHERE status = 'ACTIVE' " +
            "HAVING distance < #{radiusKm} ORDER BY distance")
    List<Warehouse> getNearbyWarehouses(@Param("longitude") Double longitude,
                                         @Param("latitude") Double latitude,
                                         @Param("radiusKm") Double radiusKm);
    
    /**
     * 获取容量预警仓库
     */
    @Select("SELECT * FROM warehouse WHERE status = 'ACTIVE' " +
            "AND (used_capacity * 1.0 / total_capacity) > #{threshold}")
    List<Warehouse> getHighUsageWarehouses(@Param("threshold") Double threshold);
    
    /**
     * 更新仓库使用容量
     */
    @Select("UPDATE warehouse SET used_capacity = used_capacity + #{delta}, " +
            "updated_at = NOW() WHERE id = #{warehouseId}")
    int updateUsedCapacity(@Param("warehouseId") Long warehouseId, 
                           @Param("delta") Integer delta);
}
