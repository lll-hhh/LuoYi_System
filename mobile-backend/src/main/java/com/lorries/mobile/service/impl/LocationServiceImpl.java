package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.dto.LocationReportRequest;
import com.lorries.mobile.entity.Driver;
import com.lorries.mobile.entity.LocationRecord;
import com.lorries.mobile.mapper.DriverMapper;
import com.lorries.mobile.mapper.LocationRecordMapper;
import com.lorries.mobile.service.LocationService;
import com.lorries.mobile.util.GeoUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 位置服务实现
 */
@Service
public class LocationServiceImpl extends ServiceImpl<LocationRecordMapper, LocationRecord> implements LocationService {

    @Autowired
    private DriverMapper driverMapper;

    @Override
    @Async("locationExecutor")
    public void reportLocation(Long driverId, Long vehicleId, LocationReportRequest request) {
        LocationRecord record = new LocationRecord();
        BeanUtils.copyProperties(request, record);
        record.setDriverId(driverId);
        record.setVehicleId(vehicleId);
        record.setRecordTime(request.getRecordTime() != null ? request.getRecordTime() : LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        save(record);
        
        // 更新司机位置
        if (driverId != null) {
            Driver driver = driverMapper.selectById(driverId);
            if (driver != null) {
                driver.setLongitude(request.getLongitude());
                driver.setLatitude(request.getLatitude());
                driver.setLastLocationTime(LocalDateTime.now());
                driverMapper.updateById(driver);
            }
        }
    }

    @Override
    @Async("locationExecutor")
    public void batchReportLocation(Long driverId, Long vehicleId, List<LocationReportRequest> requests) {
        List<LocationRecord> records = new ArrayList<>();
        for (LocationReportRequest request : requests) {
            LocationRecord record = new LocationRecord();
            BeanUtils.copyProperties(request, record);
            record.setDriverId(driverId);
            record.setVehicleId(vehicleId);
            record.setRecordTime(request.getRecordTime() != null ? request.getRecordTime() : LocalDateTime.now());
            record.setCreatedAt(LocalDateTime.now());
            records.add(record);
        }
        saveBatch(records);
        
        // 更新司机最新位置
        if (driverId != null && !requests.isEmpty()) {
            LocationReportRequest lastRequest = requests.get(requests.size() - 1);
            Driver driver = driverMapper.selectById(driverId);
            if (driver != null) {
                driver.setLongitude(lastRequest.getLongitude());
                driver.setLatitude(lastRequest.getLatitude());
                driver.setLastLocationTime(LocalDateTime.now());
                driverMapper.updateById(driver);
            }
        }
    }

    @Override
    public LocationRecord getLatestLocation(Long vehicleId) {
        LambdaQueryWrapper<LocationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationRecord::getVehicleId, vehicleId)
                .orderByDesc(LocationRecord::getRecordTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public LocationRecord getDriverLatestLocation(Long driverId) {
        LambdaQueryWrapper<LocationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationRecord::getDriverId, driverId)
                .orderByDesc(LocationRecord::getRecordTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public List<LocationRecord> getTrack(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<LocationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationRecord::getVehicleId, vehicleId)
                .ge(LocationRecord::getRecordTime, startTime)
                .le(LocationRecord::getRecordTime, endTime)
                .orderByAsc(LocationRecord::getRecordTime);
        return list(wrapper);
    }

    @Override
    public List<LocationRecord> getTaskTrack(Long taskId) {
        LambdaQueryWrapper<LocationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationRecord::getTaskId, taskId)
                .orderByAsc(LocationRecord::getRecordTime);
        return list(wrapper);
    }

    @Override
    public Double calculateMileage(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        List<LocationRecord> records = getTrack(vehicleId, startTime, endTime);
        if (records.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < records.size(); i++) {
            LocationRecord prev = records.get(i - 1);
            LocationRecord curr = records.get(i);
            double distance = GeoUtil.calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude());
            totalDistance += distance;
        }
        
        return totalDistance / 1000; // 转换为公里
    }
}
