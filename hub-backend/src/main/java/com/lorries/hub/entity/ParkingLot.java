package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车场实体
 */
@Data
@TableName("parking_lot")
public class ParkingLot {

    @TableId(type = IdType.AUTO)
    private Integer parkingLotId;
    
    private String lotName;
    
    private String address;
    
    private Integer totalSpaces;
    
    private Integer availableSpaces;
    
    private BigDecimal hourlyRate;
    
    private String status;
    
    private String contactPhone;
    
    private String openTime;
    
    private String closeTime;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
