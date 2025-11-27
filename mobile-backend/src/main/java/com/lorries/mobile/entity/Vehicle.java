package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户车辆实体
 */
@Data
@TableName("vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Integer vehicleId;

    private Integer userId;
    private Integer vehicleTypeId;
    private String plateNumber;
    private String plateColor;
    private String brand;
    private String model;
    private String color;
    private String vin;
    private String engineNumber;
    private LocalDate registerDate;
    private LocalDate insuranceExpireDate;
    private LocalDate inspectionExpireDate;
    private String transportLicense;
    private LocalDate transportLicenseExpire;
    private BigDecimal maxLoadWeight;
    private BigDecimal maxLoadVolume;
    private String status;
    private Boolean isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String vehicleTypeName;
}
