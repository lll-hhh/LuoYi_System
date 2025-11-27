package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车流量数据实体
 */
@Data
@TableName("traffic_flow")
public class TrafficFlow {

    @TableId(type = IdType.AUTO)
    private Long flowId;
    
    private Integer roadId;
    
    private Integer cameraId;
    
    private LocalDateTime recordTime;
    
    private Integer vehicleCount;
    
    private Double avgSpeed;
    
    private Double occupancy;
    
    private String direction;
    
    private String vehicleTypes;
    
    private Integer congestionLevel;
    
    private LocalDateTime createdAt;
}
