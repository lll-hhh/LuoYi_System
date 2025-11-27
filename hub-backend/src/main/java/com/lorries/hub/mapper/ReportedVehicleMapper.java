package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.ReportedVehicle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 已报备车辆Mapper
 */
@Mapper
public interface ReportedVehicleMapper extends BaseMapper<ReportedVehicle> {
    // 使用 MyBatis-Plus 提供的基础 CRUD 操作
}
