package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.entity.TrafficInfo;

import java.util.List;
import java.util.Map;

/**
 * 交通服务接口
 */
public interface TrafficService extends IService<TrafficInfo> {

    /**
     * 获取区域交通状况
     */
    Map<String, Object> getAreaTraffic(Double lat, Double lng, Double radius);

    /**
     * 获取道路交通状况
     */
    TrafficInfo getRoadTraffic(Integer roadId);

    /**
     * 获取交通事件列表
     */
    List<Map<String, Object>> getTrafficEvents(Double lat, Double lng, Double radius);

    /**
     * 上报交通事件
     */
    void reportTrafficEvent(Map<String, Object> eventInfo);

    /**
     * 获取交通预测
     */
    List<Map<String, Object>> getTrafficPrediction(Integer roadId, Integer hours);

    /**
     * 获取热力图数据
     */
    List<Map<String, Object>> getTrafficHeatmap(Double minLat, Double minLng, Double maxLat, Double maxLng);

    /**
     * 获取拥堵排行
     */
    List<Map<String, Object>> getCongestionRanking();

    /**
     * 订阅路况推送
     */
    void subscribeTrafficPush(Map<String, Object> subscribeInfo);

    /**
     * 取消订阅路况推送
     */
    void unsubscribeTrafficPush(Integer userId);
}
