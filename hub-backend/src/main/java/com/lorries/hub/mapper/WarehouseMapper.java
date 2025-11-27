package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Warehouse;
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
     * 获取仓库容量使用情况
     */
    @Select("SELECT w.warehouse_id, w.warehouse_name, w.capacity, " +
            "       COALESCE(SUM(c.volume * c.quantity), 0) as used_volume " +
            "FROM warehouse w " +
            "LEFT JOIN cargo c ON w.warehouse_id = c.warehouse_id AND c.status = 'in_stock' " +
            "WHERE w.warehouse_id = #{id} " +
            "GROUP BY w.warehouse_id, w.warehouse_name, w.capacity")
    Map<String, Object> getCapacityUsage(@Param("id") Integer id);

    /**
     * 获取所有仓库的容量使用概览
     */
    @Select("SELECT w.warehouse_id, w.warehouse_name, w.capacity, " +
            "       COALESCE(SUM(c.volume * c.quantity), 0) as used_volume, " +
            "       COUNT(DISTINCT c.cargo_id) as cargo_count " +
            "FROM warehouse w " +
            "LEFT JOIN cargo c ON w.warehouse_id = c.warehouse_id AND c.status = 'in_stock' " +
            "GROUP BY w.warehouse_id, w.warehouse_name, w.capacity")
    List<Map<String, Object>> getOverview();

    /**
     * 按仓库类型统计
     */
    @Select("SELECT warehouse_type as type, COUNT(*) as count FROM warehouse GROUP BY warehouse_type")
    List<Map<String, Object>> countByType();

    /**
     * 按状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM warehouse GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
