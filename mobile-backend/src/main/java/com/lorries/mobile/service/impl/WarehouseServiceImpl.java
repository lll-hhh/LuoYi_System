package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.Warehouse;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.mapper.WarehouseMapper;
import com.lorries.mobile.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库服务实现
 */
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Override
    public Warehouse getWarehouseDetail(Long warehouseId) {
        Warehouse warehouse = getById(warehouseId);
        if (warehouse == null) {
            throw new ResourceNotFoundException("仓库", warehouseId);
        }
        return warehouse;
    }

    @Override
    public PageResult<Warehouse> getWarehouseList(String status, String keyword, Integer page, Integer pageSize) {
        Page<Warehouse> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Warehouse::getName, keyword)
                    .or().like(Warehouse::getAddress, keyword)
                    .or().like(Warehouse::getManagerPhone, keyword));
        }
        wrapper.orderByDesc(Warehouse::getCreatedAt);
        
        Page<Warehouse> result = page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public List<Warehouse> getActiveWarehouses() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getStatus, "ACTIVE");
        return list(wrapper);
    }

    @Override
    public List<Warehouse> getNearbyWarehouses(Double longitude, Double latitude, Double radiusKm) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getStatus, "ACTIVE");
        
        // 简单过滤经纬度范围（粗筛）
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));
        
        wrapper.ge(Warehouse::getLatitude, latitude - latDelta)
                .le(Warehouse::getLatitude, latitude + latDelta)
                .ge(Warehouse::getLongitude, longitude - lngDelta)
                .le(Warehouse::getLongitude, longitude + lngDelta);
        
        return list(wrapper);
    }

    @Override
    @Transactional
    public void updateCapacity(Long warehouseId, Integer usedCapacity) {
        Warehouse warehouse = getById(warehouseId);
        if (warehouse == null) {
            throw new ResourceNotFoundException("仓库", warehouseId);
        }
        if (warehouse.getCapacity() == null) {
            throw new BusinessException("仓库总容量未设置");
        }
        if (usedCapacity > warehouse.getCapacity()) {
            throw new BusinessException("使用容量不能超过总容量");
        }
        
        warehouse.setUsedCapacity(usedCapacity);
        warehouse.setUpdatedAt(LocalDateTime.now());
        updateById(warehouse);
    }

    @Override
    public Integer getAvailableCapacity(Long warehouseId) {
        Warehouse warehouse = getById(warehouseId);
        if (warehouse == null) {
            throw new ResourceNotFoundException("仓库", warehouseId);
        }
        if (warehouse.getCapacity() == null) {
            throw new BusinessException("仓库总容量未设置");
        }
        int used = warehouse.getUsedCapacity() == null ? 0 : warehouse.getUsedCapacity();
        return warehouse.getCapacity() - used;
    }
}
