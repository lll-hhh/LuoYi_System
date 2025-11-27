package com.lorries.mobile.dto;

import lombok.Data;

/**
 * 仪表盘统计数据
 */
@Data
public class DashboardStats {

    /**
     * 今日任务数
     */
    private Integer todayTasks;

    /**
     * 进行中任务
     */
    private Integer inProgressTasks;

    /**
     * 已完成任务
     */
    private Integer completedTasks;

    /**
     * 待处理异常
     */
    private Integer pendingAnomalies;

    /**
     * 今日行驶里程（公里）
     */
    private Double todayMileage;

    /**
     * 本月行驶里程（公里）
     */
    private Double monthMileage;

    /**
     * 在线车辆数
     */
    private Integer onlineVehicles;

    /**
     * 总车辆数
     */
    private Integer totalVehicles;

    /**
     * 在线司机数
     */
    private Integer onlineDrivers;

    /**
     * 总司机数
     */
    private Integer totalDrivers;

    /**
     * 待签收货物
     */
    private Integer pendingCargos;

    /**
     * 今日送达货物
     */
    private Integer deliveredCargos;
}
