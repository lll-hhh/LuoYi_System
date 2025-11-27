package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.entity.Warehouse;

import java.util.List;

/**
 * 仓库服务接口
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 获取仓库列表
     */
    List<Warehouse> getWarehouseList(String status, String warehouseType);

    /**
     * 获取仓库详情
     */
    Warehouse getWarehouseDetail(Long warehouseId);

    /**
     * 根据位置获取最近的仓库
     */
    Warehouse getNearestWarehouse(Double longitude, Double latitude);

    /**
     * 获取指定范围内的仓库
     */
    List<Warehouse> getWarehousesInRange(Double longitude, Double latitude, Double radiusKm);

    /**
     * 更新仓库容量
     */
    void updateCapacity(Long warehouseId, Integer usedCapacity);
}
