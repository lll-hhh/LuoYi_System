package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.ParkingLot;
import com.lorries.hub.entity.ParkingRecord;
import com.lorries.hub.mapper.ParkingLotMapper;
import com.lorries.hub.mapper.ParkingRecordMapper;
import com.lorries.hub.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 停车场服务实现
 */
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot> implements ParkingService {

    private final ParkingRecordMapper parkingRecordMapper;

    @Override
    public List<ParkingLot> listLots() {
        return list();
    }

    @Override
    public ParkingLot getLotById(Integer id) {
        return getById(id);
    }

    @Override
    @Transactional
    public void saveLot(ParkingLot lot) {
        lot.setStatus("active");
        lot.setAvailableSpaces(lot.getTotalSpaces());
        save(lot);
    }

    @Override
    @Transactional
    public void updateLot(ParkingLot lot) {
        updateById(lot);
    }

    @Override
    @Transactional
    public void removeLot(Integer id) {
        removeById(id);
    }

    @Override
    public Map<String, Object> getLotStatus(Integer id) {
        return baseMapper.getStatusById(id);
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalLots", count());
        overview.put("lotList", baseMapper.getOverview());
        overview.put("byStatus", baseMapper.countByStatus());
        return overview;
    }

    // ============ 停车记录 ============

    @Override
    public PageResult<ParkingRecord> findRecordPage(Integer page, Integer size, Integer lotId, String plateNumber, String status) {
        Page<ParkingRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ParkingRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (lotId != null) {
            wrapper.eq(ParkingRecord::getParkingLotId, lotId);
        }
        if (StringUtils.hasText(plateNumber)) {
            wrapper.like(ParkingRecord::getPlateNumber, plateNumber);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ParkingRecord::getStatus, status);
        }
        
        wrapper.orderByDesc(ParkingRecord::getEntryTime);
        Page<ParkingRecord> result = parkingRecordMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    @Transactional
    public ParkingRecord vehicleEntry(Map<String, Object> entryInfo) {
        Integer lotId = (Integer) entryInfo.get("lotId");
        String plateNumber = (String) entryInfo.get("plateNumber");
        
        // 创建停车记录
        ParkingRecord record = new ParkingRecord();
        record.setParkingLotId(lotId);
        record.setPlateNumber(plateNumber);
        record.setEntryTime(LocalDateTime.now());
        record.setStatus("parked");
        if (entryInfo.containsKey("entryImageUrl")) {
            record.setEntryImageUrl((String) entryInfo.get("entryImageUrl"));
        }
        parkingRecordMapper.insert(record);
        
        // 更新停车场可用车位
        ParkingLot lot = getById(lotId);
        if (lot != null && lot.getAvailableSpaces() > 0) {
            lot.setAvailableSpaces(lot.getAvailableSpaces() - 1);
            updateById(lot);
        }
        
        return record;
    }

    @Override
    @Transactional
    public Map<String, Object> vehicleExit(Map<String, Object> exitInfo) {
        String plateNumber = (String) exitInfo.get("plateNumber");
        
        // 查找停车记录
        ParkingRecord record = parkingRecordMapper.selectParkedByPlate(plateNumber);
        if (record == null) {
            throw new RuntimeException("未找到该车辆的停车记录");
        }
        
        LocalDateTime exitTime = LocalDateTime.now();
        record.setExitTime(exitTime);
        record.setStatus("exited");
        if (exitInfo.containsKey("exitImageUrl")) {
            record.setExitImageUrl((String) exitInfo.get("exitImageUrl"));
        }
        
        // 计算费用
        ParkingLot lot = getById(record.getParkingLotId());
        BigDecimal fee = calculateFee(record.getEntryTime(), exitTime, lot.getHourlyRate());
        record.setFee(fee);
        
        if (exitInfo.containsKey("paymentMethod")) {
            record.setPaymentMethod((String) exitInfo.get("paymentMethod"));
            record.setPaymentTime(LocalDateTime.now());
        }
        
        parkingRecordMapper.updateById(record);
        
        // 更新停车场可用车位
        if (lot != null) {
            lot.setAvailableSpaces(lot.getAvailableSpaces() + 1);
            updateById(lot);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("fee", fee);
        result.put("duration", Duration.between(record.getEntryTime(), exitTime).toMinutes());
        
        return result;
    }

    @Override
    public ParkingRecord getVehicleStatus(String plateNumber) {
        return parkingRecordMapper.selectParkedByPlate(plateNumber);
    }

    @Override
    public Map<String, Object> getRevenue(Integer lotId, String startDate, String endDate) {
        Map<String, Object> revenue = new HashMap<>();
        
        if (lotId != null) {
            revenue.put("byDate", parkingRecordMapper.getRevenueByDate(lotId, startDate, endDate));
        } else {
            revenue.put("byLot", parkingRecordMapper.getRevenueByLot(startDate, endDate));
        }
        
        return revenue;
    }

    @Override
    public List<Map<String, Object>> getPeakHours(Integer lotId) {
        if (lotId != null) {
            return parkingRecordMapper.getPeakHours(lotId);
        }
        return parkingRecordMapper.getAllPeakHours();
    }

    /**
     * 计算停车费用
     */
    private BigDecimal calculateFee(LocalDateTime entryTime, LocalDateTime exitTime, BigDecimal hourlyRate) {
        if (hourlyRate == null) {
            hourlyRate = BigDecimal.valueOf(5); // 默认每小时5元
        }
        
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        // 不足一小时按一小时计算
        long hours = (minutes + 59) / 60;
        
        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }
}
