package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.entity.Route;

import java.util.List;
import java.util.Map;

/**
 * 路线服务接口
 */
public interface RouteService extends IService<Route> {

    /**
     * 获取推荐路线
     */
    List<Route> getRecommendedRoutes(Map<String, Object> routeRequest);

    /**
     * 获取历史路线
     */
    List<Route> getHistoryRoutes(Integer userId);

    /**
     * 保存路线
     */
    void saveRoute(Route route);

    /**
     * 收藏路线
     */
    void favoriteRoute(Long routeId, Integer userId);

    /**
     * 取消收藏
     */
    void unfavoriteRoute(Long routeId, Integer userId);

    /**
     * 获取收藏的路线
     */
    List<Route> getFavoriteRoutes(Integer userId);

    /**
     * 开始导航
     */
    Map<String, Object> startNavigation(Long routeId);

    /**
     * 更新导航位置
     */
    Map<String, Object> updateLocation(Long routeId, Map<String, Object> location);

    /**
     * 结束导航
     */
    void finishNavigation(Long routeId);

    /**
     * 获取实时路况
     */
    Map<String, Object> getTrafficInfo(Double startLat, Double startLng, Double endLat, Double endLng);
}
