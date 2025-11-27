package com.lorries.mobile.service.impl;

import com.lorries.mobile.entity.Vehicle;
import com.lorries.mobile.mapper.VehicleMapper;
import com.lorries.mobile.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 车辆服务实现类
 */
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleMapper vehicleMapper;

    @Override
    public List<Vehicle> getMyVehicles() {
        // 实际应从SecurityContext获取当前用户ID
        return vehicleMapper.selectList(null);
    }

    @Override
    public List<Vehicle> findByUserId(Long userId) {
        return vehicleMapper.selectByUserId(userId);
    }

    @Override
    public Vehicle findByPlateNumber(String plateNumber) {
        return vehicleMapper.selectByPlateNumber(plateNumber);
    }

    @Override
    @Transactional
    public Boolean addVehicle(Vehicle vehicle) {
        return vehicleMapper.insert(vehicle) > 0;
    }

    @Override
    @Transactional
    public Boolean updateVehicle(Vehicle vehicle) {
        return vehicleMapper.updateById(vehicle) > 0;
    }

    @Override
    @Transactional
    public Boolean deleteVehicle(Integer vehicleId) {
        return vehicleMapper.deleteById(vehicleId) > 0;
    }

    @Override
    @Transactional
    public void setDefaultVehicle(Integer vehicleId) {
        // 实现设置默认车辆逻辑
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle != null) {
            vehicle.setIsDefault(true);
            vehicleMapper.updateById(vehicle);
        }
    }
}
