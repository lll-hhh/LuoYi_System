package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Anomaly;
import com.lorries.hub.mapper.AnomalyMapper;
import com.lorries.hub.service.AnomalyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异常事件服务实现
 */
@Service
@RequiredArgsConstructor
public class AnomalyServiceImpl extends ServiceImpl<AnomalyMapper, Anomaly> implements AnomalyService {

    @Override
    public PageResult<Anomaly> findPage(Integer page, Integer size, String type, String status, String level) {
        Page<Anomaly> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Anomaly> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Anomaly::getAnomalyType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Anomaly::getStatus, status);
        }
        if (StringUtils.hasText(level)) {
            wrapper.eq(Anomaly::getAnomalyLevel, level);
        }
        
        wrapper.orderByDesc(Anomaly::getOccurredAt);
        Page<Anomaly> result = page(pageParam, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public Anomaly getById(Long id) {
        return baseMapper.selectByIdWithRelations(id);
    }

    @Override
    @Transactional
    public void saveAnomaly(Anomaly anomaly) {
        anomaly.setStatus("pending");
        anomaly.setOccurredAt(LocalDateTime.now());
        save(anomaly);
    }

    @Override
    @Transactional
    public void updateAnomaly(Anomaly anomaly) {
        updateById(anomaly);
    }

    @Override
    @Transactional
    public void removeAnomaly(Long id) {
        removeById(id);
    }

    @Override
    @Transactional
    public void handleAnomaly(Long id, Map<String, Object> handleInfo) {
        Anomaly anomaly = new Anomaly();
        anomaly.setAnomalyId(id);
        anomaly.setStatus("processing");
        anomaly.setHandledBy((Integer) handleInfo.get("handledBy"));
        anomaly.setHandledAt(LocalDateTime.now());
        if (handleInfo.containsKey("handleResult")) {
            anomaly.setHandleResult((String) handleInfo.get("handleResult"));
        }
        updateById(anomaly);
    }

    @Override
    @Transactional
    public void closeAnomaly(Long id, String remark) {
        Anomaly anomaly = new Anomaly();
        anomaly.setAnomalyId(id);
        anomaly.setStatus("closed");
        anomaly.setHandleResult(remark);
        updateById(anomaly);
    }

    @Override
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", count());
        stats.put("unhandled", baseMapper.countUnhandled());
        stats.put("byType", baseMapper.countByType());
        stats.put("byLevel", baseMapper.countByLevel());
        stats.put("byStatus", baseMapper.countByStatus());
        return stats;
    }

    @Override
    public List<Map<String, Object>> statisticsByType() {
        return baseMapper.countByType();
    }

    @Override
    public List<Anomaly> getRecentAnomalies(Integer limit) {
        return baseMapper.selectRecent(limit);
    }

    @Override
    @Transactional
    public void batchHandle(List<Long> ids, Map<String, Object> handleInfo) {
        for (Long id : ids) {
            handleAnomaly(id, handleInfo);
        }
    }

    @Override
    @Transactional
    public void batchClose(List<Long> ids, String remark) {
        for (Long id : ids) {
            closeAnomaly(id, remark);
        }
    }

    @Override
    public Long getUnhandledCount() {
        return baseMapper.countUnhandled();
    }
}
