package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.ParkingLot;
import com.lorries.hub.entity.ParkingRecord;

import java.util.List;
import java.util.Map;

/**
 * 停车场服务接口
 */
public interface ParkingService extends IService<ParkingLot> {

    /**
     * 获取停车场列表
     */
    List<ParkingLot> listLots();

    /**
     * 根据ID获取停车场
     */
    ParkingLot getLotById(Integer id);

    /**
     * 新增停车场
     */
    void saveLot(ParkingLot lot);

    /**
     * 更新停车场
     */
    void updateLot(ParkingLot lot);

    /**
     * 删除停车场
     */
    void removeLot(Integer id);

    /**
     * 获取停车场实时状态
     */
    Map<String, Object> getLotStatus(Integer id);

    /**
     * 获取所有停车场概览
     */
    Map<String, Object> getOverview();

    // ============ 停车记录 ============

    /**
     * 分页查询停车记录
     */
    PageResult<ParkingRecord> findRecordPage(Integer page, Integer size, Integer lotId, String plateNumber, String status);

    /**
     * 车辆入场
     */
    ParkingRecord vehicleEntry(Map<String, Object> entryInfo);

    /**
     * 车辆出场
     */
    Map<String, Object> vehicleExit(Map<String, Object> exitInfo);

    /**
     * 查询车辆停车状态
     */
    ParkingRecord getVehicleStatus(String plateNumber);

    /**
     * 获取收入统计
     */
    Map<String, Object> getRevenue(Integer lotId, String startDate, String endDate);

    /**
     * 获取停车高峰时段分析
     */
    List<Map<String, Object>> getPeakHours(Integer lotId);
}
