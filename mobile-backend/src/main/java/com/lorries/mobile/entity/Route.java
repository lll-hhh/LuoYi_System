package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 路线实体
 */
@Data
@TableName("route")
public class Route {

    @TableId(type = IdType.AUTO)
    private Long routeId;
    
    private Integer userId;
    
    private String routeName;
    
    private BigDecimal startLat;
    
    private BigDecimal startLng;
    
    private String startAddress;
    
    private BigDecimal endLat;
    
    private BigDecimal endLng;
    
    private String endAddress;
    
    private String waypoints;
    
    private Double distance;
    
    private Integer duration;
    
    private String routeType;
    
    private String polyline;
    
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Boolean isFavorite;
}
