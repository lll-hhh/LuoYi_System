package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.Route;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 路线Mapper
 */
@Mapper
public interface RouteMapper extends BaseMapper<Route> {

    /**
     * 获取用户历史路线
     */
    @Select("SELECT * FROM route WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 50")
    List<Route> selectHistoryByUser(@Param("userId") Integer userId);

    /**
     * 获取用户收藏的路线
     */
    @Select("SELECT r.* FROM route r " +
            "JOIN route_favorite rf ON r.route_id = rf.route_id " +
            "WHERE rf.user_id = #{userId}")
    List<Route> selectFavoritesByUser(@Param("userId") Integer userId);
}
