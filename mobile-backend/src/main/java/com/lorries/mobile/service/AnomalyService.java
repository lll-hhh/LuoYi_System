package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.AnomalyReportRequest;
import com.lorries.mobile.dto.AnomalyVO;
import com.lorries.mobile.entity.AnomalyEvent;

/**
 * 异常事件服务接口
 */
public interface AnomalyService extends IService<AnomalyEvent> {

    /**
     * 上报异常事件
     */
    AnomalyVO reportAnomaly(Long reporterId, AnomalyReportRequest request);

    /**
     * 获取异常事件详情
     */
    AnomalyVO getAnomalyDetail(Long anomalyId);

    /**
     * 获取异常事件列表
     */
    PageResult<AnomalyVO> getAnomalyList(String status, String eventType, String severity, 
                                         Integer page, Integer pageSize);

    /**
     * 获取司机上报的异常列表
     */
    PageResult<AnomalyVO> getDriverAnomalies(Long driverId, Integer page, Integer pageSize);

    /**
     * 处理异常事件
     */
    void handleAnomaly(Long anomalyId, Long handlerId);

    /**
     * 解决异常事件
     */
    void resolveAnomaly(Long anomalyId, Long handlerId, String resolution);

    /**
     * 关闭异常事件
     */
    void closeAnomaly(Long anomalyId);

    /**
     * 获取待处理异常数量
     */
    Integer getPendingCount();
}
