package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Cargo;
import com.lorries.hub.entity.Warehouse;

import java.util.List;
import java.util.Map;

/**
 * 仓库服务接口
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 获取仓库列表
     */
    List<Warehouse> listWarehouses();

    /**
     * 根据ID获取仓库
     */
    Warehouse getById(Integer id);

    /**
     * 新增仓库
     */
    void saveWarehouse(Warehouse warehouse);

    /**
     * 更新仓库
     */
    void updateWarehouse(Warehouse warehouse);

    /**
     * 删除仓库
     */
    void removeWarehouse(Integer id);

    /**
     * 获取仓库统计
     */
    Map<String, Object> getWarehouseStatistics(Integer id);

    /**
     * 获取所有仓库概览
     */
    Map<String, Object> getOverview();

    // ============ 货物管理 ============

    /**
     * 分页查询货物
     */
    PageResult<Cargo> findCargoPage(Integer page, Integer size, Integer warehouseId, String cargoType, String status);

    /**
     * 根据ID获取货物
     */
    Cargo getCargoById(Long id);

    /**
     * 新增货物（入库）
     */
    void saveCargo(Cargo cargo);

    /**
     * 更新货物信息
     */
    void updateCargo(Cargo cargo);

    /**
     * 删除货物
     */
    void removeCargo(Long id);

    /**
     * 货物入库
     */
    void cargoInbound(Cargo cargo);

    /**
     * 货物出库
     */
    void cargoOutbound(Long cargoId, Map<String, Object> outboundInfo);

    /**
     * 货物库存统计
     */
    Map<String, Object> getInventoryStatistics(Integer warehouseId);

    /**
     * 按类型统计货物
     */
    List<Map<String, Object>> statisticsByType(Integer warehouseId);
}
