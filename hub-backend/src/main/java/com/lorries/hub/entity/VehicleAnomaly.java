package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 异常记录实体
 */
@Data
@TableName("reported_vehicle_anomaly")
public class VehicleAnomaly {

    @TableId(type = IdType.AUTO)
    private Integer anomalyId;

    private Integer vehicleId;
    private Integer anomalyTypeId;
    private Integer cameraId;
    private Integer roadId;
    private String snapshotUrl;
    private String videoUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String severity;
    private String status;
    private Integer handledBy;
    private LocalDateTime handledAt;
    private String handleResult;
    private LocalDateTime occurredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 关联字段
    @TableField(exist = false)
    private String plateNumber;

    @TableField(exist = false)
    private String anomalyTypeName;

    @TableField(exist = false)
    private String cameraName;

    @TableField(exist = false)
    private String roadName;

    @TableField(exist = false)
    private String handlerName;
}
