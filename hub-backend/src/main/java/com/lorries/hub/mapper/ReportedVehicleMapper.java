package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.ReportedVehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 已报备车辆Mapper
 */
@Mapper
public interface ReportedVehicleMapper extends BaseMapper<ReportedVehicle> {

    @Select("""
            SELECT rv.*, vt.type_name as vehicle_type_name
            FROM reported_vehicle rv
            LEFT JOIN vehicle_type vt ON rv.vehicle_type_id = vt.vehicle_type_id
            WHERE rv.plate_number = #{plateNumber}
            """)
    ReportedVehicle findByPlateNumber(@Param("plateNumber") String plateNumber);
}
