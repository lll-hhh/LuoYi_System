package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 异常事件实体
 */
@Data
@TableName("anomaly_event")
public class Anomaly {

    @TableId(type = IdType.AUTO)
    private Long anomalyId;

    private String anomalyType;
    
    private String anomalyLevel;
    
    private String title;
    
    private String description;
    
    private Integer cameraId;
    
    private Integer roadId;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private String snapshotUrl;
    
    private String videoUrl;
    
    private String status;
    
    private Integer handledBy;
    
    private LocalDateTime handledAt;
    
    private String handleResult;
    
    private LocalDateTime occurredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String cameraName;

    @TableField(exist = false)
    private String roadName;

    @TableField(exist = false)
    private String handlerName;
}
