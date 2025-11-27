package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.LocationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 位置记录Mapper
 */
@Mapper
public interface LocationRecordMapper extends BaseMapper<LocationRecord> {
    
    /**
     * 获取车辆最新位置
     */
    @Select("SELECT * FROM location_record WHERE vehicle_id = #{vehicleId} " +
            "ORDER BY record_time DESC LIMIT 1")
    LocationRecord getLatestByVehicle(@Param("vehicleId") Long vehicleId);
    
    /**
     * 获取司机最新位置
     */
    @Select("SELECT * FROM location_record WHERE driver_id = #{driverId} " +
            "ORDER BY record_time DESC LIMIT 1")
    LocationRecord getLatestByDriver(@Param("driverId") Long driverId);
    
    /**
     * 获取轨迹数据
     */
    @Select("SELECT * FROM location_record WHERE vehicle_id = #{vehicleId} " +
            "AND record_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY record_time ASC")
    List<LocationRecord> getTrack(@Param("vehicleId") Long vehicleId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取区域内的车辆位置
     */
    @Select("SELECT * FROM location_record lr " +
            "INNER JOIN (SELECT vehicle_id, MAX(record_time) as max_time " +
            "FROM location_record GROUP BY vehicle_id) latest " +
            "ON lr.vehicle_id = latest.vehicle_id AND lr.record_time = latest.max_time " +
            "WHERE lr.latitude BETWEEN #{minLat} AND #{maxLat} " +
            "AND lr.longitude BETWEEN #{minLng} AND #{maxLng}")
    List<LocationRecord> getVehiclesInArea(@Param("minLat") Double minLat,
                                            @Param("maxLat") Double maxLat,
                                            @Param("minLng") Double minLng,
                                            @Param("maxLng") Double maxLng);
    
    /**
     * 按天统计位置记录数
     */
    @Select("SELECT DATE(record_time) as date, COUNT(*) as count " +
            "FROM location_record " +
            "WHERE vehicle_id = #{vehicleId} AND record_time >= #{startTime} " +
            "GROUP BY DATE(record_time)")
    List<Map<String, Object>> countByDay(@Param("vehicleId") Long vehicleId,
                                          @Param("startTime") LocalDateTime startTime);
    
    /**
     * 删除过期数据
     */
    @Select("DELETE FROM location_record WHERE record_time < #{expireTime}")
    int deleteExpired(@Param("expireTime") LocalDateTime expireTime);
}
