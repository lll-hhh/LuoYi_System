package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.Driver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 司机 Mapper
 */
@Mapper
public interface DriverMapper extends BaseMapper<Driver> {

    @Select("SELECT * FROM driver WHERE user_id = #{userId} LIMIT 1")
    Driver findByUserId(@Param("userId") Long userId);
}
