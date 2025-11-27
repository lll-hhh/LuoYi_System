package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Parking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车场Mapper
 */
@Mapper
public interface ParkingMapper extends BaseMapper<Parking> {
}
