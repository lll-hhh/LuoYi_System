package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.AnomalyReportRequest;
import com.lorries.mobile.dto.AnomalyVO;
import com.lorries.mobile.entity.AnomalyEvent;
import com.lorries.mobile.entity.Driver;
import com.lorries.mobile.entity.Vehicle;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.mapper.AnomalyEventMapper;
import com.lorries.mobile.mapper.DriverMapper;
import com.lorries.mobile.mapper.VehicleMapper;
import com.lorries.mobile.service.AnomalyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 异常事件服务实现
 */
@Service
public class AnomalyServiceImpl extends ServiceImpl<AnomalyEventMapper, AnomalyEvent> implements AnomalyService {

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public AnomalyVO reportAnomaly(Long reporterId, AnomalyReportRequest request) {
        AnomalyEvent event = new AnomalyEvent();
        BeanUtils.copyProperties(request, event);
        
        // 生成事件编号
        String eventNo = "A" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        event.setEventNo(eventNo);
        event.setStatus("PENDING");
        event.setReportedBy(reporterId);
        event.setReportedAt(LocalDateTime.now());
        event.setCreatedAt(LocalDateTime.now());
        
        // 处理图片列表
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                event.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        }
        
        // 获取司机信息
        Driver driver = driverMapper.selectById(reporterId);
        if (driver != null) {
            event.setDriverId(driver.getId());
            event.setVehicleId(driver.getCurrentVehicleId());
        }
        
        save(event);
        return convertToVO(event);
    }

    @Override
    public AnomalyVO getAnomalyDetail(Long anomalyId) {
        AnomalyEvent event = getById(anomalyId);
        if (event == null) {
            throw new ResourceNotFoundException("异常事件", anomalyId);
        }
        return convertToVO(event);
    }

    @Override
    public PageResult<AnomalyVO> getAnomalyList(String status, String eventType, String severity,
                                                 Integer page, Integer pageSize) {
        Page<AnomalyEvent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AnomalyEvent> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AnomalyEvent::getStatus, status);
        }
        if (eventType != null && !eventType.isEmpty()) {
            wrapper.eq(AnomalyEvent::getEventType, eventType);
        }
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(AnomalyEvent::getSeverity, severity);
        }
        wrapper.orderByDesc(AnomalyEvent::getCreatedAt);
        
        Page<AnomalyEvent> result = page(pageParam, wrapper);
        List<AnomalyVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public PageResult<AnomalyVO> getDriverAnomalies(Long driverId, Integer page, Integer pageSize) {
        Page<AnomalyEvent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AnomalyEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnomalyEvent::getReportedBy, driverId)
                .orderByDesc(AnomalyEvent::getCreatedAt);
        
        Page<AnomalyEvent> result = page(pageParam, wrapper);
        List<AnomalyVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(list, result.getTotal());
    }

    @Override
    @Transactional
    public void handleAnomaly(Long anomalyId, Long handlerId) {
        AnomalyEvent event = getById(anomalyId);
        if (event == null) {
            throw new ResourceNotFoundException("异常事件", anomalyId);
        }
        if (!"PENDING".equals(event.getStatus())) {
            throw new BusinessException("只有待处理状态的事件可以开始处理");
        }
        
        event.setStatus("PROCESSING");
        event.setHandledBy(handlerId);
        event.setUpdatedAt(LocalDateTime.now());
        updateById(event);
    }

    @Override
    @Transactional
    public void resolveAnomaly(Long anomalyId, Long handlerId, String resolution) {
        AnomalyEvent event = getById(anomalyId);
        if (event == null) {
            throw new ResourceNotFoundException("异常事件", anomalyId);
        }
        
        event.setStatus("RESOLVED");
        event.setHandledBy(handlerId);
        event.setHandledAt(LocalDateTime.now());
        event.setResolution(resolution);
        event.setUpdatedAt(LocalDateTime.now());
        updateById(event);
    }

    @Override
    @Transactional
    public void closeAnomaly(Long anomalyId) {
        AnomalyEvent event = getById(anomalyId);
        if (event == null) {
            throw new ResourceNotFoundException("异常事件", anomalyId);
        }
        
        event.setStatus("CLOSED");
        event.setUpdatedAt(LocalDateTime.now());
        updateById(event);
    }

    @Override
    public Integer getPendingCount() {
        LambdaQueryWrapper<AnomalyEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnomalyEvent::getStatus, "PENDING");
        return Math.toIntExact(count(wrapper));
    }

    private AnomalyVO convertToVO(AnomalyEvent event) {
        AnomalyVO vo = new AnomalyVO();
        BeanUtils.copyProperties(event, vo);
        
        vo.setEventTypeName(getEventTypeName(event.getEventType()));
        vo.setSeverityName(getSeverityName(event.getSeverity()));
        vo.setStatusName(getStatusName(event.getStatus()));
        
        // 解析图片
        if (event.getImages() != null && !event.getImages().isEmpty()) {
            try {
                vo.setImages(objectMapper.readValue(event.getImages(), String[].class));
            } catch (JsonProcessingException ignored) {
            }
        }
        
        // 关联信息
        if (event.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(event.getVehicleId());
            if (vehicle != null) {
                vo.setVehiclePlate(vehicle.getPlateNumber());
            }
        }
        if (event.getDriverId() != null) {
            Driver driver = driverMapper.selectById(event.getDriverId());
            if (driver != null) {
                vo.setDriverName(driver.getName());
            }
        }
        
        return vo;
    }

    private String getEventTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "ACCIDENT": return "事故";
            case "DELAY": return "延误";
            case "DAMAGE": return "货损";
            case "VIOLATION": return "违章";
            case "EQUIPMENT": return "设备故障";
            default: return type;
        }
    }

    private String getSeverityName(String severity) {
        if (severity == null) return "";
        switch (severity) {
            case "LOW": return "低";
            case "MEDIUM": return "中";
            case "HIGH": return "高";
            case "CRITICAL": return "严重";
            default: return severity;
        }
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待处理";
            case "PROCESSING": return "处理中";
            case "RESOLVED": return "已解决";
            case "CLOSED": return "已关闭";
            default: return status;
        }
    }
}
