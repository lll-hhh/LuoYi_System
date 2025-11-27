package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Cargo;
import com.lorries.hub.entity.Warehouse;
import com.lorries.hub.mapper.CargoMapper;
import com.lorries.hub.mapper.WarehouseMapper;
import com.lorries.hub.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final CargoMapper cargoMapper;

    @Override
    public List<Warehouse> listWarehouses() {
        return list();
    }

    @Override
    public Warehouse getById(Integer id) {
        return super.getById(id);
    }

    @Override
    @Transactional
    public void saveWarehouse(Warehouse warehouse) {
        warehouse.setStatus("active");
        save(warehouse);
    }

    @Override
    @Transactional
    public void updateWarehouse(Warehouse warehouse) {
        updateById(warehouse);
    }

    @Override
    @Transactional
    public void removeWarehouse(Integer id) {
        removeById(id);
    }

    @Override
    public Map<String, Object> getWarehouseStatistics(Integer id) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("capacityUsage", baseMapper.getCapacityUsage(id));
        stats.put("inventory", cargoMapper.getInventoryStatistics(id));
        stats.put("cargoByType", cargoMapper.statisticsByType(id));
        return stats;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalWarehouses", count());
        overview.put("warehouseList", baseMapper.getOverview());
        overview.put("byType", baseMapper.countByType());
        overview.put("byStatus", baseMapper.countByStatus());
        return overview;
    }

    // ============ 货物管理 ============

    @Override
    public PageResult<Cargo> findCargoPage(Integer page, Integer size, Integer warehouseId, String cargoType, String status) {
        Page<Cargo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Cargo> wrapper = new LambdaQueryWrapper<>();
        
        if (warehouseId != null) {
            wrapper.eq(Cargo::getWarehouseId, warehouseId);
        }
        if (StringUtils.hasText(cargoType)) {
            wrapper.eq(Cargo::getCargoType, cargoType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Cargo::getStatus, status);
        }
        
        wrapper.orderByDesc(Cargo::getCreatedAt);
        Page<Cargo> result = cargoMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public Cargo getCargoById(Long id) {
        return cargoMapper.selectById(id);
    }

    @Override
    @Transactional
    public void saveCargo(Cargo cargo) {
        cargo.setStatus("in_stock");
        cargo.setInboundTime(LocalDateTime.now());
        cargoMapper.insert(cargo);
    }

    @Override
    @Transactional
    public void updateCargo(Cargo cargo) {
        cargoMapper.updateById(cargo);
    }

    @Override
    @Transactional
    public void removeCargo(Long id) {
        cargoMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void cargoInbound(Cargo cargo) {
        cargo.setStatus("in_stock");
        cargo.setInboundTime(LocalDateTime.now());
        cargoMapper.insert(cargo);
    }

    @Override
    @Transactional
    public void cargoOutbound(Long cargoId, Map<String, Object> outboundInfo) {
        Cargo cargo = cargoMapper.selectById(cargoId);
        if (cargo != null) {
            cargo.setStatus("out_stock");
            cargo.setOutboundTime(LocalDateTime.now());
            if (outboundInfo.containsKey("receiver")) {
                cargo.setReceiver((String) outboundInfo.get("receiver"));
            }
            cargoMapper.updateById(cargo);
        }
    }

    @Override
    public Map<String, Object> getInventoryStatistics(Integer warehouseId) {
        if (warehouseId != null) {
            return cargoMapper.getInventoryStatistics(warehouseId);
        }
        
        // 所有仓库的汇总
        Map<String, Object> stats = new HashMap<>();
        stats.put("byWarehouse", cargoMapper.getInventoryByWarehouse());
        return stats;
    }

    @Override
    public List<Map<String, Object>> statisticsByType(Integer warehouseId) {
        return cargoMapper.statisticsByType(warehouseId);
    }
}
