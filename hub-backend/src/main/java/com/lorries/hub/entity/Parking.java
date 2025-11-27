package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车场实体
 */
@Data
@TableName("parking")
public class Parking {

    @TableId(type = IdType.AUTO)
    private Integer parkingId;

    private String parkingName;
    private String parkingCode;
    private String parkingType;
    private String address;
    private Integer totalSpaces;
    private Integer availableSpaces;
    private String contactPhone;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
