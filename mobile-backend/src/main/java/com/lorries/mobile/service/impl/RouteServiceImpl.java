package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.entity.Route;
import com.lorries.mobile.mapper.RouteMapper;
import com.lorries.mobile.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路线服务实现
 */
@Service
@RequiredArgsConstructor
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

    @Override
    public List<Route> getRecommendedRoutes(Map<String, Object> routeRequest) {
        // 简化实现：返回推荐路线
        List<Route> routes = new ArrayList<>();
        
        Double startLat = (Double) routeRequest.get("startLat");
        Double startLng = (Double) routeRequest.get("startLng");
        Double endLat = (Double) routeRequest.get("endLat");
        Double endLng = (Double) routeRequest.get("endLng");
        
        // 创建推荐路线
        Route route = new Route();
        route.setStartLat(BigDecimal.valueOf(startLat));
        route.setStartLng(BigDecimal.valueOf(startLng));
        route.setEndLat(BigDecimal.valueOf(endLat));
        route.setEndLng(BigDecimal.valueOf(endLng));
        route.setRouteType("fastest");
        route.setDistance(calculateDistance(startLat, startLng, endLat, endLng));
        route.setDuration(estimateDuration(route.getDistance()));
        
        routes.add(route);
        
        return routes;
    }

    @Override
    public List<Route> getHistoryRoutes(Integer userId) {
        return baseMapper.selectHistoryByUser(userId);
    }

    @Override
    @Transactional
    public void saveRoute(Route route) {
        route.setStatus("active");
        save(route);
    }

    @Override
    @Transactional
    public void favoriteRoute(Long routeId, Integer userId) {
        // 简化实现：可以创建 route_favorite 表存储
        // 这里直接更新路线的收藏状态
    }

    @Override
    @Transactional
    public void unfavoriteRoute(Long routeId, Integer userId) {
        // 简化实现
    }

    @Override
    public List<Route> getFavoriteRoutes(Integer userId) {
        return baseMapper.selectFavoritesByUser(userId);
    }

    @Override
    public Map<String, Object> startNavigation(Long routeId) {
        Route route = getById(routeId);
        Map<String, Object> result = new HashMap<>();
        result.put("route", route);
        result.put("status", "navigating");
        result.put("startTime", System.currentTimeMillis());
        return result;
    }

    @Override
    public Map<String, Object> updateLocation(Long routeId, Map<String, Object> location) {
        Map<String, Object> result = new HashMap<>();
        result.put("currentLocation", location);
        result.put("remainingDistance", 0);
        result.put("remainingTime", 0);
        return result;
    }

    @Override
    @Transactional
    public void finishNavigation(Long routeId) {
        Route route = new Route();
        route.setRouteId(routeId);
        route.setStatus("completed");
        updateById(route);
    }

    @Override
    public Map<String, Object> getTrafficInfo(Double startLat, Double startLng, Double endLat, Double endLng) {
        Map<String, Object> trafficInfo = new HashMap<>();
        trafficInfo.put("congestionLevel", 2);
        trafficInfo.put("avgSpeed", 45.0);
        trafficInfo.put("estimatedTime", estimateDuration(calculateDistance(startLat, startLng, endLat, endLng)));
        return trafficInfo;
    }

    /**
     * 计算两点间距离（简化实现）
     */
    private Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        double R = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 估算行驶时间（分钟）
     */
    private Integer estimateDuration(Double distance) {
        // 假设平均速度40km/h
        return (int) (distance / 40 * 60);
    }
}
