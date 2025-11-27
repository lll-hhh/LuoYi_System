package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.TrafficFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 车流量Mapper
 */
@Mapper
public interface TrafficFlowMapper extends BaseMapper<TrafficFlow> {

    /**
     * 获取指定道路的实时车流量
     */
    @Select("SELECT * FROM traffic_flow WHERE road_id = #{roadId} " +
            "ORDER BY record_time DESC LIMIT 10")
    List<TrafficFlow> selectRealtimeByRoadId(@Param("roadId") Integer roadId);

    /**
     * 获取所有道路的最新车流量
     */
    @Select("SELECT tf.* FROM traffic_flow tf " +
            "INNER JOIN (SELECT road_id, MAX(record_time) as max_time " +
            "            FROM traffic_flow GROUP BY road_id) latest " +
            "ON tf.road_id = latest.road_id AND tf.record_time = latest.max_time")
    List<TrafficFlow> selectLatestAll();

    /**
     * 按小时统计车流量
     */
    @Select("SELECT EXTRACT(HOUR FROM record_time) as hour, " +
            "       SUM(vehicle_count) as total_count, " +
            "       AVG(avg_speed) as avg_speed " +
            "FROM traffic_flow " +
            "WHERE road_id = #{roadId} AND DATE(record_time) = #{date} " +
            "GROUP BY EXTRACT(HOUR FROM record_time) " +
            "ORDER BY hour")
    List<Map<String, Object>> statisticsByHour(@Param("roadId") Integer roadId, @Param("date") String date);

    /**
     * 按车辆类型统计
     */
    @Select("SELECT vehicle_types as type, SUM(vehicle_count) as count " +
            "FROM traffic_flow " +
            "WHERE road_id = #{roadId} AND DATE(record_time) = #{date} " +
            "GROUP BY vehicle_types")
    List<Map<String, Object>> statisticsByVehicleType(@Param("roadId") Integer roadId, @Param("date") String date);

    /**
     * 获取拥堵统计
     */
    @Select("SELECT congestion_level as level, COUNT(*) as count " +
            "FROM traffic_flow " +
            "WHERE road_id = #{roadId} " +
            "GROUP BY congestion_level")
    List<Map<String, Object>> statisticsByCongestion(@Param("roadId") Integer roadId);
}
