package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 车辆Mapper
 */
@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {

    @Select("""
            SELECT v.*, vt.type_name as vehicle_type_name
            FROM vehicle v
            LEFT JOIN vehicle_type vt ON v.vehicle_type_id = vt.vehicle_type_id
            WHERE v.user_id = #{userId}
            ORDER BY v.is_default DESC, v.created_at DESC
            """)
    List<Vehicle> findByUserId(@Param("userId") Integer userId);

    @Select("""
            SELECT * FROM vehicle WHERE user_id = #{userId}
            ORDER BY is_default DESC, created_at DESC
            """)
    List<Vehicle> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM vehicle WHERE plate_number = #{plateNumber}")
    Vehicle selectByPlateNumber(@Param("plateNumber") String plateNumber);
}
