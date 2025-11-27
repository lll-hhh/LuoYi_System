package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 已报备车辆实体
 */
@Data
@TableName("reported_vehicle")
public class ReportedVehicle {

    @TableId(type = IdType.AUTO)
    private Integer vehicleId;

    private Integer vehicleTypeId;
    private String plateNumber;
    private String plateColor;
    private String ownerName;
    private String ownerPhone;
    private String companyName;
    private String transportLicense;
    private BigDecimal maxLoadWeight;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String vehicleTypeName;
}
