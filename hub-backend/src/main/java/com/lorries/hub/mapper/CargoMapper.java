package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Cargo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 货物Mapper
 */
@Mapper
public interface CargoMapper extends BaseMapper<Cargo> {

    /**
     * 按类型统计货物
     */
    @Select("SELECT cargo_type as type, COUNT(*) as count, " +
            "       SUM(quantity) as total_quantity, " +
            "       SUM(weight * quantity) as total_weight " +
            "FROM cargo " +
            "WHERE warehouse_id = #{warehouseId} AND status = 'in_stock' " +
            "GROUP BY cargo_type")
    List<Map<String, Object>> statisticsByType(@Param("warehouseId") Integer warehouseId);

    /**
     * 获取库存统计
     */
    @Select("SELECT COUNT(*) as cargo_count, " +
            "       SUM(quantity) as total_quantity, " +
            "       SUM(weight * quantity) as total_weight, " +
            "       SUM(volume * quantity) as total_volume, " +
            "       SUM(unit_price * quantity) as total_value " +
            "FROM cargo " +
            "WHERE warehouse_id = #{warehouseId} AND status = 'in_stock'")
    Map<String, Object> getInventoryStatistics(@Param("warehouseId") Integer warehouseId);

    /**
     * 获取所有仓库的库存统计
     */
    @Select("SELECT w.warehouse_id, w.warehouse_name, " +
            "       COUNT(c.cargo_id) as cargo_count, " +
            "       COALESCE(SUM(c.quantity), 0) as total_quantity " +
            "FROM warehouse w " +
            "LEFT JOIN cargo c ON w.warehouse_id = c.warehouse_id AND c.status = 'in_stock' " +
            "GROUP BY w.warehouse_id, w.warehouse_name")
    List<Map<String, Object>> getInventoryByWarehouse();
}
