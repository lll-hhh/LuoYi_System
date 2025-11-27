package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.TrafficFlow;
import com.lorries.hub.mapper.TrafficFlowMapper;
import com.lorries.hub.service.TrafficFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 车流量服务实现
 */
@Service
@RequiredArgsConstructor
public class TrafficFlowServiceImpl extends ServiceImpl<TrafficFlowMapper, TrafficFlow> implements TrafficFlowService {

    @Override
    public PageResult<TrafficFlow> findPage(Integer page, Integer size, Integer roadId, String startTime, String endTime) {
        Page<TrafficFlow> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TrafficFlow> wrapper = new LambdaQueryWrapper<>();
        
        if (roadId != null) {
            wrapper.eq(TrafficFlow::getRoadId, roadId);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(TrafficFlow::getRecordTime, LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(TrafficFlow::getRecordTime, LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        wrapper.orderByDesc(TrafficFlow::getRecordTime);
        Page<TrafficFlow> result = page(pageParam, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public TrafficFlow getById(Long id) {
        return super.getById(id);
    }

    @Override
    public void saveTrafficFlow(TrafficFlow trafficFlow) {
        trafficFlow.setCreatedAt(LocalDateTime.now());
        save(trafficFlow);
    }

    @Override
    public List<TrafficFlow> getRealtime(Integer roadId) {
        if (roadId != null) {
            return baseMapper.selectRealtimeByRoadId(roadId);
        }
        return baseMapper.selectLatestAll();
    }

    @Override
    public List<Map<String, Object>> getHistory(Integer roadId, String startDate, String endDate, String granularity) {
        // 根据粒度返回历史数据
        List<Map<String, Object>> result = new ArrayList<>();
        
        if ("hour".equals(granularity)) {
            result = baseMapper.statisticsByHour(roadId, startDate);
        }
        // 可以扩展其他粒度的统计
        
        return result;
    }

    @Override
    public List<Map<String, Object>> predict(Integer roadId, Integer hours) {
        // 简单的预测实现：基于历史平均值
        List<Map<String, Object>> predictions = new ArrayList<>();
        List<TrafficFlow> recentData = getRealtime(roadId);
        
        if (recentData.isEmpty()) {
            return predictions;
        }
        
        // 计算平均值
        double avgCount = recentData.stream()
                .mapToInt(TrafficFlow::getVehicleCount)
                .average()
                .orElse(0);
        double avgSpeed = recentData.stream()
                .mapToDouble(TrafficFlow::getAvgSpeed)
                .average()
                .orElse(0);
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= hours; i++) {
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("time", now.plusHours(i).toString());
            prediction.put("predictedCount", (int) avgCount);
            prediction.put("predictedSpeed", avgSpeed);
            predictions.add(prediction);
        }
        
        return predictions;
    }

    @Override
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<TrafficFlow> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(TrafficFlow::getRecordTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(TrafficFlow::getRecordTime, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        
        List<TrafficFlow> flows = list(wrapper);
        
        stats.put("totalRecords", flows.size());
        stats.put("totalVehicles", flows.stream().mapToInt(TrafficFlow::getVehicleCount).sum());
        stats.put("avgSpeed", flows.stream().mapToDouble(TrafficFlow::getAvgSpeed).average().orElse(0));
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getHeatmap(String time) {
        // 返回热力图数据：各道路的车流量
        List<TrafficFlow> latestFlows = baseMapper.selectLatestAll();
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        for (TrafficFlow flow : latestFlows) {
            Map<String, Object> point = new HashMap<>();
            point.put("roadId", flow.getRoadId());
            point.put("count", flow.getVehicleCount());
            point.put("congestionLevel", flow.getCongestionLevel());
            heatmapData.add(point);
        }
        
        return heatmapData;
    }

    @Override
    public List<Map<String, Object>> getPeakHours(Integer roadId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return baseMapper.statisticsByHour(roadId, today);
    }

    @Override
    public List<Map<String, Object>> statisticsByVehicleType(Integer roadId, String date) {
        return baseMapper.statisticsByVehicleType(roadId, date);
    }

    @Override
    public Map<String, Object> getCongestionAnalysis(Integer roadId) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("byCongestionLevel", baseMapper.statisticsByCongestion(roadId));
        
        List<TrafficFlow> recentData = getRealtime(roadId);
        if (!recentData.isEmpty()) {
            TrafficFlow latest = recentData.get(0);
            analysis.put("currentLevel", latest.getCongestionLevel());
            analysis.put("currentSpeed", latest.getAvgSpeed());
            analysis.put("currentCount", latest.getVehicleCount());
        }
        
        return analysis;
    }
}
