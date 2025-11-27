package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 摄像头实体
 */
@Data
@TableName("camera")
public class Camera {

    @TableId(type = IdType.AUTO)
    private Integer cameraId;

    private Integer roadId;
    private Integer junctionId;
    private String cameraName;
    private String cameraCode;
    private String ipAddress;
    private Integer port;
    private String streamUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String direction;
    private String cameraType;
    private String onlineStatus;
    private LocalDateTime lastHeartbeat;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String roadName;

    @TableField(exist = false)
    private String junctionName;
}
