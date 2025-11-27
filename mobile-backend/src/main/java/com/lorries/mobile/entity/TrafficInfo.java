package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交通信息实体
 */
@Data
@TableName("traffic_info")
public class TrafficInfo {

    @TableId(type = IdType.AUTO)
    private Long trafficId;
    
    private Integer roadId;
    
    private Integer congestionLevel;
    
    private Double avgSpeed;
    
    private Integer vehicleCount;
    
    private String direction;
    
    private String status;
    
    private LocalDateTime recordTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String roadName;
}
