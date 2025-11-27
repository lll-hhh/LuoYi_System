package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 路口实体
 */
@Data
@TableName("junction")
public class Junction {

    @TableId(type = IdType.AUTO)
    private Integer junctionId;

    private String junctionName;
    private String junctionCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String signalControl;
    private String description;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
