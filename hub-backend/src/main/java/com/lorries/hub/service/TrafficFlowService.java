package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.TrafficFlow;

import java.util.List;
import java.util.Map;

/**
 * 车流量服务接口
 */
public interface TrafficFlowService extends IService<TrafficFlow> {

    /**
     * 分页查询车流量数据
     */
    PageResult<TrafficFlow> findPage(Integer page, Integer size, Integer roadId, String startTime, String endTime);

    /**
     * 根据ID获取车流量数据
     */
    TrafficFlow getById(Long id);

    /**
     * 新增车流量数据
     */
    void saveTrafficFlow(TrafficFlow trafficFlow);

    /**
     * 获取实时车流量
     */
    List<TrafficFlow> getRealtime(Integer roadId);

    /**
     * 获取历史趋势
     */
    List<Map<String, Object>> getHistory(Integer roadId, String startDate, String endDate, String granularity);

    /**
     * 车流量预测
     */
    List<Map<String, Object>> predict(Integer roadId, Integer hours);

    /**
     * 获取车流量统计
     */
    Map<String, Object> getStatistics(String startDate, String endDate);

    /**
     * 获取热力图数据
     */
    List<Map<String, Object>> getHeatmap(String time);

    /**
     * 获取高峰时段
     */
    List<Map<String, Object>> getPeakHours(Integer roadId);

    /**
     * 按车辆类型统计
     */
    List<Map<String, Object>> statisticsByVehicleType(Integer roadId, String date);

    /**
     * 获取拥堵分析
     */
    Map<String, Object> getCongestionAnalysis(Integer roadId);
}
