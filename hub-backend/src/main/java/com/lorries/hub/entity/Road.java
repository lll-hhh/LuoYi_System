package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 道路实体
 */
@Data
@TableName("road")
public class Road {

    @TableId(type = IdType.AUTO)
    private Integer roadId;

    private String roadName;
    private String roadCode;
    private String roadLevel;
    private Integer laneCount;
    private BigDecimal speedLimit;
    private BigDecimal length;
    private String direction;
    private BigDecimal startLat;
    private BigDecimal startLng;
    private BigDecimal endLat;
    private BigDecimal endLng;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
