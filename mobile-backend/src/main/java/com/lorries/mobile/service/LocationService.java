package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.LocationReportRequest;
import com.lorries.mobile.entity.LocationRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 位置服务接口
 */
public interface LocationService extends IService<LocationRecord> {

    /**
     * 上报位置
     */
    void reportLocation(Long driverId, Long vehicleId, LocationReportRequest request);

    /**
     * 批量上报位置
     */
    void batchReportLocation(Long driverId, Long vehicleId, List<LocationReportRequest> requests);

    /**
     * 获取最新位置
     */
    LocationRecord getLatestLocation(Long vehicleId);

    /**
     * 获取司机最新位置
     */
    LocationRecord getDriverLatestLocation(Long driverId);

    /**
     * 获取轨迹
     */
    List<LocationRecord> getTrack(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取任务轨迹
     */
    List<LocationRecord> getTaskTrack(Long taskId);

    /**
     * 计算行驶里程
     */
    Double calculateMileage(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);
}
