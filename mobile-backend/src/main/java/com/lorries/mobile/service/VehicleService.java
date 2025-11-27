package com.lorries.mobile.service;

import com.lorries.mobile.entity.Vehicle;

import java.util.List;

/**
 * 车辆服务接口
 */
public interface VehicleService {

    List<Vehicle> getMyVehicles();

    List<Vehicle> findByUserId(Long userId);

    Vehicle findByPlateNumber(String plateNumber);

    Boolean addVehicle(Vehicle vehicle);

    Boolean updateVehicle(Vehicle vehicle);

    Boolean deleteVehicle(Integer vehicleId);

    void setDefaultVehicle(Integer vehicleId);
}
