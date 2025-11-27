package com.lorries.mobile.service;

import com.lorries.mobile.dto.DashboardStats;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取仪表盘统计数据
     */
    DashboardStats getDashboardStats();

    /**
     * 获取司机仪表盘统计
     */
    DashboardStats getDriverDashboardStats(Long driverId);

    /**
     * 获取今日任务统计
     */
    Integer getTodayTaskCount();

    /**
     * 获取今日行驶里程
     */
    Double getTodayMileage(Long driverId);

    /**
     * 获取本月行驶里程
     */
    Double getMonthMileage(Long driverId);

    /**
     * 获取在线车辆数
     */
    Integer getOnlineVehicleCount();

    /**
     * 获取在线司机数
     */
    Integer getOnlineDriverCount();
}
