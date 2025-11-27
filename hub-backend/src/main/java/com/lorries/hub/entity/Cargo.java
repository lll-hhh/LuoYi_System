package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物实体
 */
@Data
@TableName("cargo")
public class Cargo {

    @TableId(type = IdType.AUTO)
    private Long cargoId;
    
    private Integer warehouseId;
    
    private String cargoName;
    
    private String cargoType;
    
    private String cargoCode;
    
    private Double weight;
    
    private Double volume;
    
    private Integer quantity;
    
    private String unit;
    
    private BigDecimal unitPrice;
    
    private String status;
    
    private String location;
    
    private LocalDateTime inboundTime;
    
    private LocalDateTime outboundTime;
    
    private String shipper;
    
    private String receiver;
    
    private String remark;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
