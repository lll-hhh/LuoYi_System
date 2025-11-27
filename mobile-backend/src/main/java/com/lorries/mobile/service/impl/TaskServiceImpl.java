package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.TaskCreateRequest;
import com.lorries.mobile.dto.TaskVO;
import com.lorries.mobile.entity.TransportTask;
import com.lorries.mobile.entity.Driver;
import com.lorries.mobile.entity.Vehicle;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.mapper.TaskMapper;
import com.lorries.mobile.mapper.DriverMapper;
import com.lorries.mobile.mapper.VehicleMapper;
import com.lorries.mobile.service.TaskService;
import com.lorries.mobile.util.GeoUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 任务服务实现
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TransportTask> implements TaskService {

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    @Transactional
    public TaskVO createTask(TaskCreateRequest request, Long createdBy) {
        TransportTask task = new TransportTask();
        BeanUtils.copyProperties(request, task);
        
        // 生成任务编号
        String taskNo = "T" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        task.setTaskNo(taskNo);
        task.setStatus("PENDING");
        task.setCreatedBy(createdBy);
        task.setCreatedAt(LocalDateTime.now());
        
        // 计算预计距离
        double distance = GeoUtil.calculateDistance(
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude());
        task.setEstimatedDistance(distance / 1000); // 转换为公里
        
        save(task);
        return convertToVO(task);
    }

    @Override
    public TaskVO getTaskDetail(Long taskId) {
        TransportTask task = getById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务", taskId);
        }
        return convertToVO(task);
    }

    @Override
    public PageResult<TaskVO> getDriverTasks(Long driverId, String status, Integer page, Integer pageSize) {
        Page<TransportTask> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportTask::getDriverId, driverId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(TransportTask::getStatus, status);
        }
        wrapper.orderByDesc(TransportTask::getCreatedAt);
        
        Page<TransportTask> result = page(pageParam, wrapper);
        List<TaskVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(list, result.getTotal());
    }

    @Override
    @Transactional
    public void assignTask(Long taskId, Long driverId, Long vehicleId) {
        TransportTask task = getById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务", taskId);
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException("只有待分配状态的任务可以分配");
        }
        
        task.setDriverId(driverId);
        task.setVehicleId(vehicleId);
        task.setStatus("ASSIGNED");
        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);
        
        // 更新司机状态
        Driver driver = driverMapper.selectById(driverId);
        if (driver != null) {
            driver.setCurrentTaskId(taskId);
            driver.setCurrentVehicleId(vehicleId);
            driver.setStatus("BUSY");
            driverMapper.updateById(driver);
        }
    }

    @Override
    @Transactional
    public void startTask(Long taskId, Long driverId) {
        TransportTask task = getById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务", taskId);
        }
        if (!driverId.equals(task.getDriverId())) {
            throw new BusinessException("无权操作此任务");
        }
        if (!"ASSIGNED".equals(task.getStatus())) {
            throw new BusinessException("只有已分配状态的任务可以开始");
        }
        
        task.setStatus("IN_PROGRESS");
        task.setActualStartTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);
    }

    @Override
    @Transactional
    public void completeTask(Long taskId, Long driverId, Double actualDistance) {
        TransportTask task = getById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务", taskId);
        }
        if (!driverId.equals(task.getDriverId())) {
            throw new BusinessException("无权操作此任务");
        }
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException("只有进行中状态的任务可以完成");
        }
        
        task.setStatus("COMPLETED");
        task.setActualEndTime(LocalDateTime.now());
        task.setActualDistance(actualDistance);
        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);
        
        // 更新司机状态
        Driver driver = driverMapper.selectById(driverId);
        if (driver != null) {
            driver.setCurrentTaskId(null);
            driver.setStatus("AVAILABLE");
            driver.setCompletedTasks(driver.getCompletedTasks() + 1);
            driver.setTotalMileage(driver.getTotalMileage() + actualDistance);
            driverMapper.updateById(driver);
        }
    }

    @Override
    @Transactional
    public void cancelTask(Long taskId, String reason) {
        TransportTask task = getById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务", taskId);
        }
        if ("COMPLETED".equals(task.getStatus()) || "CANCELLED".equals(task.getStatus())) {
            throw new BusinessException("任务已完成或已取消");
        }
        
        task.setStatus("CANCELLED");
        task.setRemark(reason);
        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);
        
        // 释放司机
        if (task.getDriverId() != null) {
            Driver driver = driverMapper.selectById(task.getDriverId());
            if (driver != null && taskId.equals(driver.getCurrentTaskId())) {
                driver.setCurrentTaskId(null);
                driver.setStatus("AVAILABLE");
                driverMapper.updateById(driver);
            }
        }
    }

    @Override
    public TaskVO getCurrentTask(Long driverId) {
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportTask::getDriverId, driverId)
                .eq(TransportTask::getStatus, "IN_PROGRESS")
                .last("LIMIT 1");
        TransportTask task = getOne(wrapper);
        return task != null ? convertToVO(task) : null;
    }

    private TaskVO convertToVO(TransportTask task) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);
        
        // 设置类型名称
        vo.setTaskTypeName(getTaskTypeName(task.getTaskType()));
        vo.setStatusName(getStatusName(task.getStatus()));
        vo.setPriorityName(getPriorityName(task.getPriority()));
        
        // 关联信息
        if (task.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(task.getVehicleId());
            if (vehicle != null) {
                vo.setVehiclePlate(vehicle.getPlateNumber());
            }
        }
        if (task.getDriverId() != null) {
            Driver driver = driverMapper.selectById(task.getDriverId());
            if (driver != null) {
                vo.setDriverName(driver.getName());
            }
        }
        
        return vo;
    }

    private String getTaskTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "DELIVERY": return "配送";
            case "PICKUP": return "取货";
            case "TRANSFER": return "调拨";
            default: return type;
        }
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待分配";
            case "ASSIGNED": return "已分配";
            case "IN_PROGRESS": return "进行中";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }

    private String getPriorityName(String priority) {
        if (priority == null) return "";
        switch (priority) {
            case "LOW": return "低";
            case "MEDIUM": return "中";
            case "HIGH": return "高";
            case "URGENT": return "紧急";
            default: return priority;
        }
    }
}
