package com.lorries.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.hub.entity.Anomaly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 异常事件Mapper
 */
@Mapper
public interface AnomalyMapper extends BaseMapper<Anomaly> {

    /**
     * 查询异常事件（带关联信息）
     */
    @Select("SELECT a.*, c.camera_name, r.road_name, e.real_name as handler_name " +
            "FROM anomaly_event a " +
            "LEFT JOIN camera c ON a.camera_id = c.camera_id " +
            "LEFT JOIN road r ON a.road_id = r.road_id " +
            "LEFT JOIN employee e ON a.handled_by = e.employee_id " +
            "WHERE a.anomaly_id = #{id}")
    Anomaly selectByIdWithRelations(@Param("id") Long id);

    /**
     * 按类型统计
     */
    @Select("SELECT anomaly_type as type, COUNT(*) as count FROM anomaly_event GROUP BY anomaly_type")
    List<Map<String, Object>> countByType();

    /**
     * 按级别统计
     */
    @Select("SELECT anomaly_level as level, COUNT(*) as count FROM anomaly_event GROUP BY anomaly_level")
    List<Map<String, Object>> countByLevel();

    /**
     * 按状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM anomaly_event GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 获取未处理异常数量
     */
    @Select("SELECT COUNT(*) FROM anomaly_event WHERE status = 'pending'")
    Long countUnhandled();

    /**
     * 获取最近的异常事件
     */
    @Select("SELECT a.*, c.camera_name, r.road_name " +
            "FROM anomaly_event a " +
            "LEFT JOIN camera c ON a.camera_id = c.camera_id " +
            "LEFT JOIN road r ON a.road_id = r.road_id " +
            "ORDER BY a.occurred_at DESC LIMIT #{limit}")
    List<Anomaly> selectRecent(@Param("limit") Integer limit);
}
