package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物申报实体
 */
@Data
@TableName("cargo_declaration")
public class CargoDeclaration {

    @TableId(type = IdType.AUTO)
    private Integer declarationId;

    private Integer userId;
    private Integer vehicleId;
    private Integer declarationTypeId;
    private String declarationNo;
    private String cargoName;
    private Integer cargoQuantity;
    private BigDecimal cargoWeight;
    private BigDecimal cargoVolume;
    private BigDecimal cargoValue;
    private Integer originHubId;
    private String originAddress;
    private Integer destinationHubId;
    private String destinationAddress;
    private LocalDateTime expectedDeparture;
    private LocalDateTime expectedArrival;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private Integer routeRecommendationId;
    private String specialRequirements;
    private String attachedDocuments;
    private String status;
    private String reviewResult;
    private String reviewComment;
    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String declarationTypeName;

    @TableField(exist = false)
    private String vehiclePlate;

    @TableField(exist = false)
    private String originHubName;

    @TableField(exist = false)
    private String destinationHubName;
}
