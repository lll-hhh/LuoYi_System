package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.ParkingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 停车记录Mapper
 */
@Mapper
public interface ParkingRecordMapper extends BaseMapper<ParkingRecord> {

    /**
     * 查询正在停放的车辆
     */
    @Select("SELECT * FROM parking_record WHERE plate_number = #{plateNumber} AND status = 'parked'")
    ParkingRecord selectParkedByPlate(@Param("plateNumber") String plateNumber);

    /**
     * 获取收入统计
     */
    @Select("SELECT DATE(exit_time) as date, SUM(fee) as total_fee, COUNT(*) as record_count " +
            "FROM parking_record " +
            "WHERE parking_lot_id = #{lotId} " +
            "AND exit_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(exit_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getRevenueByDate(@Param("lotId") Integer lotId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 获取所有停车场的收入统计
     */
    @Select("SELECT pl.lot_name, SUM(pr.fee) as total_fee, COUNT(*) as record_count " +
            "FROM parking_record pr " +
            "JOIN parking_lot pl ON pr.parking_lot_id = pl.parking_lot_id " +
            "WHERE pr.exit_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY pl.parking_lot_id, pl.lot_name")
    List<Map<String, Object>> getRevenueByLot(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 获取高峰时段统计
     */
    @Select("SELECT EXTRACT(HOUR FROM entry_time) as hour, COUNT(*) as count " +
            "FROM parking_record " +
            "WHERE parking_lot_id = #{lotId} " +
            "GROUP BY EXTRACT(HOUR FROM entry_time) " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getPeakHours(@Param("lotId") Integer lotId);

    /**
     * 获取所有停车场的高峰时段
     */
    @Select("SELECT EXTRACT(HOUR FROM entry_time) as hour, COUNT(*) as count " +
            "FROM parking_record " +
            "GROUP BY EXTRACT(HOUR FROM entry_time) " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getAllPeakHours();
}
