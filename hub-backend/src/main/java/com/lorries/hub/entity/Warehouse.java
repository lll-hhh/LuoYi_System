package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓库实体
 */
@Data
@TableName("warehouse")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Integer warehouseId;

    private String warehouseName;
    private String warehouseCode;
    private String warehouseType;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal capacity;
    private String contactPerson;
    private String contactPhone;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
