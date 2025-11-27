package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.DriverVO;
import com.lorries.mobile.entity.Driver;

import java.util.List;

/**
 * 司机服务接口
 */
public interface DriverService extends IService<Driver> {

    /**
     * 根据用户ID获取司机信息
     */
    DriverVO getByUserId(Long userId);

    /**
     * 获取司机详情
     */
    DriverVO getDriverDetail(Long driverId);

    /**
     * 获取司机列表
     */
    PageResult<DriverVO> getDriverList(String status, String keyword, Integer page, Integer pageSize);

    /**
     * 更新司机状态
     */
    void updateStatus(Long driverId, String status);

    /**
     * 更新司机位置
     */
    void updateLocation(Long driverId, Double longitude, Double latitude);

    /**
     * 绑定车辆
     */
    void bindVehicle(Long driverId, Long vehicleId);

    /**
     * 解绑车辆
     */
    void unbindVehicle(Long driverId);

    /**
     * 获取空闲司机列表
     */
    List<DriverVO> getAvailableDrivers();

    /**
     * 获取司机完成任务数
     */
    Integer getCompletedTaskCount(Long driverId);

    /**
     * 获取司机累计里程
     */
    Double getTotalMileage(Long driverId);
}
