package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.Warehouse;

import java.util.List;

/**
 * 仓库服务接口
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 获取仓库列表
     */
    PageResult<Warehouse> getWarehouseList(String status, String keyword, Integer page, Integer pageSize);

    /**
     * 获取仓库详情
     */
    Warehouse getWarehouseDetail(Long warehouseId);

    /**
     * 获取活跃仓库
     */
    List<Warehouse> getActiveWarehouses();

    /**
     * 获取附近仓库
     */
    List<Warehouse> getNearbyWarehouses(Double longitude, Double latitude, Double radiusKm);

    /**
     * 更新仓库容量
     */
    void updateCapacity(Long warehouseId, Integer usedCapacity);

    /**
     * 获取仓库可用容量
     */
    Integer getAvailableCapacity(Long warehouseId);
}
