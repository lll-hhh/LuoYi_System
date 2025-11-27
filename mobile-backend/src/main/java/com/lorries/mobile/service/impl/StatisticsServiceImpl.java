package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lorries.mobile.dto.DashboardStats;
import com.lorries.mobile.entity.*;
import com.lorries.mobile.mapper.*;
import com.lorries.mobile.service.LocationService;
import com.lorries.mobile.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 统计服务实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private CargoMapper cargoMapper;

    @Autowired
    private AnomalyEventMapper anomalyEventMapper;

    @Autowired
    private LocationService locationService;

    @Override
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        
        // 今日任务数
        LambdaQueryWrapper<TransportTask> todayTaskWrapper = new LambdaQueryWrapper<>();
        todayTaskWrapper.ge(TransportTask::getCreatedAt, todayStart)
                .le(TransportTask::getCreatedAt, todayEnd);
        stats.setTodayTasks(Math.toIntExact(taskMapper.selectCount(todayTaskWrapper)));
        
        // 进行中任务
        LambdaQueryWrapper<TransportTask> inProgressWrapper = new LambdaQueryWrapper<>();
        inProgressWrapper.eq(TransportTask::getStatus, "IN_PROGRESS");
        stats.setInProgressTasks(Math.toIntExact(taskMapper.selectCount(inProgressWrapper)));
        
        // 已完成任务
        LambdaQueryWrapper<TransportTask> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(TransportTask::getStatus, "COMPLETED")
                .ge(TransportTask::getActualEndTime, todayStart);
        stats.setCompletedTasks(Math.toIntExact(taskMapper.selectCount(completedWrapper)));
        
        // 待处理异常
        LambdaQueryWrapper<AnomalyEvent> pendingAnomalyWrapper = new LambdaQueryWrapper<>();
        pendingAnomalyWrapper.eq(AnomalyEvent::getStatus, "PENDING");
        stats.setPendingAnomalies(Math.toIntExact(anomalyEventMapper.selectCount(pendingAnomalyWrapper)));
        
        // 在线车辆
        LambdaQueryWrapper<Vehicle> onlineVehicleWrapper = new LambdaQueryWrapper<>();
        onlineVehicleWrapper.eq(Vehicle::getStatus, "RUNNING");
        stats.setOnlineVehicles(Math.toIntExact(vehicleMapper.selectCount(onlineVehicleWrapper)));
        stats.setTotalVehicles(Math.toIntExact(vehicleMapper.selectCount(null)));
        
        // 在线司机
        LambdaQueryWrapper<Driver> onlineDriverWrapper = new LambdaQueryWrapper<>();
        onlineDriverWrapper.in(Driver::getStatus, "AVAILABLE", "BUSY");
        stats.setOnlineDrivers(Math.toIntExact(driverMapper.selectCount(onlineDriverWrapper)));
        stats.setTotalDrivers(Math.toIntExact(driverMapper.selectCount(null)));
        
        // 待签收货物
        LambdaQueryWrapper<Cargo> pendingCargoWrapper = new LambdaQueryWrapper<>();
        pendingCargoWrapper.in(Cargo::getStatus, "TRANSIT", "ARRIVED");
        stats.setPendingCargos(Math.toIntExact(cargoMapper.selectCount(pendingCargoWrapper)));
        
        // 今日送达货物
        LambdaQueryWrapper<Cargo> deliveredWrapper = new LambdaQueryWrapper<>();
        deliveredWrapper.eq(Cargo::getStatus, "DELIVERED")
                .ge(Cargo::getSignedAt, todayStart);
        stats.setDeliveredCargos(Math.toIntExact(cargoMapper.selectCount(deliveredWrapper)));
        
        return stats;
    }

    @Override
    public DashboardStats getDriverDashboardStats(Long driverId) {
        DashboardStats stats = new DashboardStats();
        
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        // 司机今日任务
        LambdaQueryWrapper<TransportTask> todayTaskWrapper = new LambdaQueryWrapper<>();
        todayTaskWrapper.eq(TransportTask::getDriverId, driverId)
                .ge(TransportTask::getCreatedAt, todayStart);
        stats.setTodayTasks(Math.toIntExact(taskMapper.selectCount(todayTaskWrapper)));
        
        // 进行中任务
        LambdaQueryWrapper<TransportTask> inProgressWrapper = new LambdaQueryWrapper<>();
        inProgressWrapper.eq(TransportTask::getDriverId, driverId)
                .eq(TransportTask::getStatus, "IN_PROGRESS");
        stats.setInProgressTasks(Math.toIntExact(taskMapper.selectCount(inProgressWrapper)));
        
        // 已完成任务
        LambdaQueryWrapper<TransportTask> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(TransportTask::getDriverId, driverId)
                .eq(TransportTask::getStatus, "COMPLETED")
                .ge(TransportTask::getActualEndTime, todayStart);
        stats.setCompletedTasks(Math.toIntExact(taskMapper.selectCount(completedWrapper)));
        
        // 待处理异常
        LambdaQueryWrapper<AnomalyEvent> pendingAnomalyWrapper = new LambdaQueryWrapper<>();
        pendingAnomalyWrapper.eq(AnomalyEvent::getReportedBy, driverId)
                .eq(AnomalyEvent::getStatus, "PENDING");
        stats.setPendingAnomalies(Math.toIntExact(anomalyEventMapper.selectCount(pendingAnomalyWrapper)));
        
        // 获取司机信息用于计算里程
        Driver driver = driverMapper.selectById(driverId);
        if (driver != null && driver.getCurrentVehicleId() != null) {
            // 今日里程
            stats.setTodayMileage(locationService.calculateMileage(
                    driver.getCurrentVehicleId(), todayStart, LocalDateTime.now()));
            // 本月里程
            stats.setMonthMileage(locationService.calculateMileage(
                    driver.getCurrentVehicleId(), monthStart, LocalDateTime.now()));
        }
        
        return stats;
    }

    @Override
    public Integer getTodayTaskCount() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(TransportTask::getCreatedAt, todayStart);
        return Math.toIntExact(taskMapper.selectCount(wrapper));
    }

    @Override
    public Double getTodayMileage(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null || driver.getCurrentVehicleId() == null) {
            return 0.0;
        }
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return locationService.calculateMileage(driver.getCurrentVehicleId(), todayStart, LocalDateTime.now());
    }

    @Override
    public Double getMonthMileage(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null || driver.getCurrentVehicleId() == null) {
            return 0.0;
        }
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        return locationService.calculateMileage(driver.getCurrentVehicleId(), monthStart, LocalDateTime.now());
    }

    @Override
    public Integer getOnlineVehicleCount() {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getStatus, "RUNNING");
        return Math.toIntExact(vehicleMapper.selectCount(wrapper));
    }

    @Override
    public Integer getOnlineDriverCount() {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Driver::getStatus, "AVAILABLE", "BUSY");
        return Math.toIntExact(driverMapper.selectCount(wrapper));
    }
}
