package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.ParkingLot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 停车场Mapper
 */
@Mapper
public interface ParkingLotMapper extends BaseMapper<ParkingLot> {

    /**
     * 获取停车场实时状态
     */
    @Select("SELECT p.*, " +
            "       (SELECT COUNT(*) FROM parking_record pr " +
            "        WHERE pr.parking_lot_id = p.parking_lot_id AND pr.status = 'parked') as current_parked " +
            "FROM parking_lot p WHERE p.parking_lot_id = #{id}")
    Map<String, Object> getStatusById(@Param("id") Integer id);

    /**
     * 获取所有停车场概览
     */
    @Select("SELECT p.parking_lot_id, p.lot_name, p.total_spaces, p.available_spaces, " +
            "       (SELECT COUNT(*) FROM parking_record pr " +
            "        WHERE pr.parking_lot_id = p.parking_lot_id AND pr.status = 'parked') as current_parked " +
            "FROM parking_lot p WHERE p.status = 'active'")
    List<Map<String, Object>> getOverview();

    /**
     * 按状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM parking_lot GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
