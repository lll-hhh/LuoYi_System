package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车记录实体
 */
@Data
@TableName("parking_record")
public class ParkingRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;
    
    private Integer parkingLotId;
    
    private String plateNumber;
    
    private LocalDateTime entryTime;
    
    private LocalDateTime exitTime;
    
    private BigDecimal fee;
    
    private String status;
    
    private String entryImageUrl;
    
    private String exitImageUrl;
    
    private String paymentMethod;
    
    private LocalDateTime paymentTime;
    
    private LocalDateTime createdAt;
}
