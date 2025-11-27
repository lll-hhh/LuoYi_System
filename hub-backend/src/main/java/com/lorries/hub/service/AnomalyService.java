package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Anomaly;

import java.util.List;
import java.util.Map;

/**
 * 异常事件服务接口
 */
public interface AnomalyService extends IService<Anomaly> {

    /**
     * 分页查询异常事件
     */
    PageResult<Anomaly> findPage(Integer page, Integer size, String type, String status, String level);

    /**
     * 根据ID获取异常事件
     */
    Anomaly getById(Long id);

    /**
     * 新增异常事件
     */
    void saveAnomaly(Anomaly anomaly);

    /**
     * 更新异常事件
     */
    void updateAnomaly(Anomaly anomaly);

    /**
     * 删除异常事件
     */
    void removeAnomaly(Long id);

    /**
     * 处理异常事件
     */
    void handleAnomaly(Long id, Map<String, Object> handleInfo);

    /**
     * 关闭异常事件
     */
    void closeAnomaly(Long id, String remark);

    /**
     * 获取异常统计
     */
    Map<String, Object> getStatistics(String startDate, String endDate);

    /**
     * 按类型统计
     */
    List<Map<String, Object>> statisticsByType();

    /**
     * 获取最近异常事件
     */
    List<Anomaly> getRecentAnomalies(Integer limit);

    /**
     * 批量处理异常事件
     */
    void batchHandle(List<Long> ids, Map<String, Object> handleInfo);

    /**
     * 批量关闭异常事件
     */
    void batchClose(List<Long> ids, String remark);

    /**
     * 获取未处理异常数量
     */
    Long getUnhandledCount();
}
