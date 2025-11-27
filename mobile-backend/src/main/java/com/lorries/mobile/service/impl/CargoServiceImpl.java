package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.CargoVO;
import com.lorries.mobile.entity.Cargo;
import com.lorries.mobile.entity.Warehouse;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.mapper.CargoMapper;
import com.lorries.mobile.mapper.WarehouseMapper;
import com.lorries.mobile.service.CargoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 货物服务实现
 */
@Service
public class CargoServiceImpl extends ServiceImpl<CargoMapper, Cargo> implements CargoService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public CargoVO getByTrackingNo(String trackingNo) {
        LambdaQueryWrapper<Cargo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cargo::getTrackingNo, trackingNo);
        Cargo cargo = getOne(wrapper);
        if (cargo == null) {
            throw new ResourceNotFoundException("货物追踪号 " + trackingNo + " 不存在");
        }
        return convertToVO(cargo);
    }

    @Override
    public CargoVO getCargoDetail(Long cargoId) {
        Cargo cargo = getById(cargoId);
        if (cargo == null) {
            throw new ResourceNotFoundException("货物", cargoId);
        }
        return convertToVO(cargo);
    }

    @Override
    public PageResult<CargoVO> getCargoList(String status, String cargoType, String keyword,
                                            Integer page, Integer pageSize) {
        Page<Cargo> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Cargo> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Cargo::getStatus, status);
        }
        if (cargoType != null && !cargoType.isEmpty()) {
            wrapper.eq(Cargo::getCargoType, cargoType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Cargo::getTrackingNo, keyword)
                    .or().like(Cargo::getName, keyword)
                    .or().like(Cargo::getReceiverName, keyword)
                    .or().like(Cargo::getReceiverPhone, keyword));
        }
        wrapper.orderByDesc(Cargo::getCreatedAt);
        
        Page<Cargo> result = page(pageParam, wrapper);
        List<CargoVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(list, result.getTotal());
    }

    @Override
    @Transactional
    public void updateStatus(Long cargoId, String status, String currentLocation) {
        Cargo cargo = getById(cargoId);
        if (cargo == null) {
            throw new ResourceNotFoundException("货物", cargoId);
        }
        
        cargo.setStatus(status);
        if (currentLocation != null) {
            cargo.setCurrentLocation(currentLocation);
        }
        cargo.setUpdatedAt(LocalDateTime.now());
        updateById(cargo);
    }

    @Override
    @Transactional
    public void signCargo(Long cargoId, String signedBy, String signatureImage) {
        Cargo cargo = getById(cargoId);
        if (cargo == null) {
            throw new ResourceNotFoundException("货物", cargoId);
        }
        if ("DELIVERED".equals(cargo.getStatus())) {
            throw new BusinessException("货物已签收");
        }
        
        cargo.setStatus("DELIVERED");
        cargo.setSignedBy(signedBy);
        cargo.setSignedAt(LocalDateTime.now());
        cargo.setSignatureImage(signatureImage);
        cargo.setActualArrival(LocalDateTime.now());
        cargo.setUpdatedAt(LocalDateTime.now());
        updateById(cargo);
    }

    @Override
    @Transactional
    public void scanInbound(String trackingNo, Long warehouseId, String location) {
        LambdaQueryWrapper<Cargo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cargo::getTrackingNo, trackingNo);
        Cargo cargo = getOne(wrapper);
        if (cargo == null) {
            throw new ResourceNotFoundException("货物追踪号 " + trackingNo + " 不存在");
        }
        
        cargo.setStatus("ARRIVED");
        cargo.setWarehouseId(warehouseId);
        cargo.setCurrentLocation(location);
        cargo.setActualArrival(LocalDateTime.now());
        cargo.setUpdatedAt(LocalDateTime.now());
        updateById(cargo);
        
        // 更新仓库使用容量
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse != null) {
            warehouse.setUsedCapacity(warehouse.getUsedCapacity() + 1);
            warehouseMapper.updateById(warehouse);
        }
    }

    @Override
    @Transactional
    public void scanOutbound(String trackingNo, Long taskId) {
        LambdaQueryWrapper<Cargo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cargo::getTrackingNo, trackingNo);
        Cargo cargo = getOne(wrapper);
        if (cargo == null) {
            throw new ResourceNotFoundException("货物追踪号 " + trackingNo + " 不存在");
        }
        
        Long warehouseId = cargo.getWarehouseId();
        
        cargo.setStatus("TRANSIT");
        cargo.setWarehouseId(null);
        cargo.setCurrentLocation("运输中");
        cargo.setUpdatedAt(LocalDateTime.now());
        updateById(cargo);
        
        // 更新仓库使用容量
        if (warehouseId != null) {
            Warehouse warehouse = warehouseMapper.selectById(warehouseId);
            if (warehouse != null && warehouse.getUsedCapacity() > 0) {
                warehouse.setUsedCapacity(warehouse.getUsedCapacity() - 1);
                warehouseMapper.updateById(warehouse);
            }
        }
    }

    private CargoVO convertToVO(Cargo cargo) {
        CargoVO vo = new CargoVO();
        BeanUtils.copyProperties(cargo, vo);
        
        vo.setCargoTypeName(getCargoTypeName(cargo.getCargoType()));
        vo.setStatusName(getStatusName(cargo.getStatus()));
        
        if (cargo.getWarehouseId() != null) {
            Warehouse warehouse = warehouseMapper.selectById(cargo.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getName());
            }
        }
        
        return vo;
    }

    private String getCargoTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "GENERAL": return "普通";
            case "FRAGILE": return "易碎";
            case "PERISHABLE": return "易腐";
            case "DANGEROUS": return "危险品";
            case "VALUABLE": return "贵重";
            default: return type;
        }
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待发";
            case "TRANSIT": return "运输中";
            case "ARRIVED": return "已到达";
            case "DELIVERED": return "已签收";
            default: return status;
        }
    }
}
