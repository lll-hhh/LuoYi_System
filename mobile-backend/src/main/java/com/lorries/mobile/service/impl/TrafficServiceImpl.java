package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.entity.TrafficInfo;
import com.lorries.mobile.mapper.TrafficInfoMapper;
import com.lorries.mobile.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 交通服务实现
 */
@Service
@RequiredArgsConstructor
public class TrafficServiceImpl extends ServiceImpl<TrafficInfoMapper, TrafficInfo> implements TrafficService {

    @Override
    public Map<String, Object> getAreaTraffic(Double lat, Double lng, Double radius) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> center = new HashMap<>();
        center.put("lat", lat);
        center.put("lng", lng);
        result.put("center", center);
        result.put("radius", radius);
        result.put("avgCongestionLevel", 2);
        result.put("totalRoads", 15);
        result.put("congestedRoads", 3);
        return result;
    }

    @Override
    public TrafficInfo getRoadTraffic(Integer roadId) {
        return baseMapper.selectLatestByRoadId(roadId);
    }

    @Override
    public List<Map<String, Object>> getTrafficEvents(Double lat, Double lng, Double radius) {
        // 简化实现：返回模拟的交通事件
        List<Map<String, Object>> events = new ArrayList<>();
        
        Map<String, Object> event1 = new HashMap<>();
        event1.put("eventId", 1);
        event1.put("type", "accident");
        event1.put("description", "轻微交通事故");
        event1.put("lat", lat + 0.01);
        event1.put("lng", lng + 0.01);
        event1.put("severity", "low");
        events.add(event1);
        
        return events;
    }

    @Override
    public void reportTrafficEvent(Map<String, Object> eventInfo) {
        // 保存交通事件上报
        // 简化实现
    }

    @Override
    public List<Map<String, Object>> getTrafficPrediction(Integer roadId, Integer hours) {
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        for (int i = 1; i <= hours; i++) {
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("hour", i);
            prediction.put("predictedCongestion", (i % 4) + 1);
            prediction.put("predictedSpeed", 30 + (i % 20));
            predictions.add(prediction);
        }
        
        return predictions;
    }

    @Override
    public List<Map<String, Object>> getTrafficHeatmap(Double minLat, Double minLng, Double maxLat, Double maxLng) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        // 简化实现：生成网格热力图数据
        double latStep = (maxLat - minLat) / 10;
        double lngStep = (maxLng - minLng) / 10;
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Map<String, Object> point = new HashMap<>();
                point.put("lat", minLat + i * latStep);
                point.put("lng", minLng + j * lngStep);
                point.put("intensity", Math.random() * 100);
                heatmapData.add(point);
            }
        }
        
        return heatmapData;
    }

    @Override
    public List<Map<String, Object>> getCongestionRanking() {
        return baseMapper.selectCongestionRanking();
    }

    @Override
    public void subscribeTrafficPush(Map<String, Object> subscribeInfo) {
        // 简化实现：保存订阅信息
    }

    @Override
    public void unsubscribeTrafficPush(Integer userId) {
        // 简化实现：删除订阅信息
    }
}
