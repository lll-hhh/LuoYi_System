package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.DriverVO;
import com.lorries.mobile.entity.Driver;
import com.lorries.mobile.entity.Vehicle;
import com.lorries.mobile.entity.TransportTask;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.mapper.DriverMapper;
import com.lorries.mobile.mapper.VehicleMapper;
import com.lorries.mobile.mapper.TaskMapper;
import com.lorries.mobile.service.DriverService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 司机服务实现
 */
@Service
public class DriverServiceImpl extends ServiceImpl<DriverMapper, Driver> implements DriverService {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public DriverVO getDriverDetail(Long driverId) {
        Driver driver = getById(driverId);
        if (driver == null) {
            throw new ResourceNotFoundException("司机", driverId);
        }
        return convertToVO(driver);
    }

    @Override
    public DriverVO getByUserId(Long userId) {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getUserId, userId);
        Driver driver = getOne(wrapper);
        if (driver == null) {
            throw new ResourceNotFoundException("司机用户ID " + userId + " 不存在");
        }
        return convertToVO(driver);
    }

    @Override
    public PageResult<DriverVO> getDriverList(String status, String keyword, Integer page, Integer pageSize) {
        Page<Driver> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Driver::getStatus, status);
        }
    if (keyword != null && !keyword.isEmpty()) {
        wrapper.and(w -> w.like(Driver::getName, keyword)
            .or().like(Driver::getPhone, keyword)
            .or().like(Driver::getLicenseNo, keyword));
        }
        wrapper.orderByDesc(Driver::getCreatedAt);
        
        Page<Driver> result = page(pageParam, wrapper);
        List<DriverVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public List<DriverVO> getAvailableDrivers() {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getStatus, "AVAILABLE");
        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long driverId, String status) {
        Driver driver = getById(driverId);
        if (driver == null) {
            throw new ResourceNotFoundException("司机", driverId);
        }
        
        // 状态检查
        if ("OFFLINE".equals(status) && driver.getCurrentTaskId() != null) {
            throw new BusinessException("请先完成当前任务后再下线");
        }
        
        driver.setStatus(status);
        driver.setUpdatedAt(LocalDateTime.now());
        updateById(driver);
    }

    @Override
    @Transactional
    public void updateLocation(Long driverId, Double longitude, Double latitude) {
        Driver driver = getById(driverId);
        if (driver == null) {
            throw new ResourceNotFoundException("司机", driverId);
        }
        
        driver.setLongitude(longitude);
        driver.setLatitude(latitude);
        driver.setLastLocationTime(LocalDateTime.now());
        updateById(driver);
    }

    @Override
    @Transactional
    public void bindVehicle(Long driverId, Long vehicleId) {
        Driver driver = getById(driverId);
        if (driver == null) {
            throw new ResourceNotFoundException("司机", driverId);
        }
        
        // 检查车辆是否已被其他司机绑定
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Driver::getCurrentVehicleId, vehicleId)
                .ne(Driver::getId, driverId);
        if (count(wrapper) > 0) {
            throw new BusinessException("该车辆已被其他司机绑定");
        }
        
        driver.setCurrentVehicleId(vehicleId);
        driver.setUpdatedAt(LocalDateTime.now());
        updateById(driver);
    }

    @Override
    @Transactional
    public void unbindVehicle(Long driverId) {
        Driver driver = getById(driverId);
        if (driver == null) {
            throw new ResourceNotFoundException("司机", driverId);
        }
        if (driver.getCurrentTaskId() != null) {
            throw new BusinessException("任务进行中无法解绑车辆");
        }
        
        driver.setCurrentVehicleId(null);
        driver.setUpdatedAt(LocalDateTime.now());
        updateById(driver);
    }

    @Override
    public Integer getCompletedTaskCount(Long driverId) {
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportTask::getDriverId, driverId)
                .eq(TransportTask::getStatus, "COMPLETED");
        return Math.toIntExact(taskMapper.selectCount(wrapper));
    }

    @Override
    public Double getTotalMileage(Long driverId) {
        Driver driver = getById(driverId);
        return driver != null ? driver.getTotalMileage() : 0.0;
    }

    private DriverVO convertToVO(Driver driver) {
        DriverVO vo = new DriverVO();
        BeanUtils.copyProperties(driver, vo);
        
        vo.setStatusName(getStatusName(driver.getStatus()));
        
        if (driver.getCurrentVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(driver.getCurrentVehicleId());
            if (vehicle != null) {
                vo.setCurrentVehiclePlate(vehicle.getPlateNumber());
                vo.setCurrentVehicleType(vehicle.getVehicleTypeName());
            }
        }
        
        return vo;
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "AVAILABLE": return "空闲";
            case "BUSY": return "任务中";
            case "OFFLINE": return "离线";
            default: return status;
        }
    }
}
