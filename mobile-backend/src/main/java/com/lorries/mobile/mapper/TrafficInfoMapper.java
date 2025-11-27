package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.TrafficInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 交通信息Mapper
 */
@Mapper
public interface TrafficInfoMapper extends BaseMapper<TrafficInfo> {

    /**
     * 获取道路最新交通信息
     */
    @Select("SELECT ti.*, r.road_name FROM traffic_info ti " +
            "JOIN road r ON ti.road_id = r.road_id " +
            "WHERE ti.road_id = #{roadId} " +
            "ORDER BY ti.record_time DESC LIMIT 1")
    TrafficInfo selectLatestByRoadId(@Param("roadId") Integer roadId);

    /**
     * 获取拥堵排行
     */
    @Select("SELECT r.road_id, r.road_name, ti.congestion_level, ti.avg_speed " +
            "FROM traffic_info ti " +
            "JOIN road r ON ti.road_id = r.road_id " +
            "WHERE ti.record_time = (SELECT MAX(record_time) FROM traffic_info WHERE road_id = ti.road_id) " +
            "ORDER BY ti.congestion_level DESC LIMIT 10")
    List<Map<String, Object>> selectCongestionRanking();
}
