package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lorries.hub.entity.VehicleAnomaly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 异常记录Mapper
 */
@Mapper
public interface VehicleAnomalyMapper extends BaseMapper<VehicleAnomaly> {

    @Select("<script>" +
            "SELECT a.*, rv.plate_number, at.type_name as anomaly_type_name, " +
            "c.camera_name, r.road_name, e.real_name as handler_name " +
            "FROM reported_vehicle_anomaly a " +
            "LEFT JOIN reported_vehicle rv ON a.vehicle_id = rv.vehicle_id " +
            "LEFT JOIN traffic_anomaly_type at ON a.anomaly_type_id = at.anomaly_type_id " +
            "LEFT JOIN camera c ON a.camera_id = c.camera_id " +
            "LEFT JOIN road r ON a.road_id = r.road_id " +
            "LEFT JOIN employee e ON a.handled_by = e.employee_id " +
            "WHERE 1=1 " +
            "<if test=\"status != null\">AND a.status = #{status}</if> " +
            "<if test=\"severity != null\">AND a.severity = #{severity}</if> " +
            "<if test=\"anomalyTypeId != null\">AND a.anomaly_type_id = #{anomalyTypeId}</if> " +
            "ORDER BY a.occurred_at DESC" +
            "</script>")
    IPage<VehicleAnomaly> findPage(Page<VehicleAnomaly> page,
                                    @Param("status") String status,
                                    @Param("severity") String severity,
                                    @Param("anomalyTypeId") Integer anomalyTypeId);
}
