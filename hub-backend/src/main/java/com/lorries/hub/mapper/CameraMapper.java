package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lorries.hub.entity.Camera;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 摄像头Mapper
 */
@Mapper
public interface CameraMapper extends BaseMapper<Camera> {

    @Select("""
            SELECT c.*, r.road_name, j.junction_name
            FROM camera c
            LEFT JOIN road r ON c.road_id = r.road_id
            LEFT JOIN junction j ON c.junction_id = j.junction_id
            WHERE c.camera_id = #{cameraId}
            """)
    Camera findById(@Param("cameraId") Integer cameraId);

    @Select("""
            <script>
            SELECT c.*, r.road_name, j.junction_name
            FROM camera c
            LEFT JOIN road r ON c.road_id = r.road_id
            LEFT JOIN junction j ON c.junction_id = j.junction_id
            WHERE 1=1
            <if test="roadId != null">AND c.road_id = #{roadId}</if>
            <if test="status != null">AND c.status = #{status}</if>
            <if test="onlineStatus != null">AND c.online_status = #{onlineStatus}</if>
            ORDER BY c.camera_id DESC
            </script>
            """)
    IPage<Camera> findPage(Page<Camera> page, 
                           @Param("roadId") Integer roadId,
                           @Param("status") String status,
                           @Param("onlineStatus") String onlineStatus);
}
