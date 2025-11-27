package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Camera;
import org.apache.ibatis.annotations.Mapper;

/**
 * 摄像头Mapper
 */
@Mapper
public interface CameraMapper extends BaseMapper<Camera> {
    // 使用 MyBatis-Plus 提供的基础 CRUD 操作
}
