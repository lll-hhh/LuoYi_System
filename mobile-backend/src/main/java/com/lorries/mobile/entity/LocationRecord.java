package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 位置记录实体
 */
@Data
@TableName("location_record")
public class LocationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车辆ID
     */
    private Long vehicleId;

    /**
     * 司机ID
     */
    private Long driverId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 海拔（米）
     */
    private Double altitude;

    /**
     * 速度（km/h）
     */
    private Double speed;

    /**
     * 方向（0-360度）
     */
    private Double direction;

    /**
     * 精度（米）
     */
    private Double accuracy;

    /**
     * 定位类型：GPS, NETWORK, WIFI
     */
    private String locationType;

    /**
     * 地址信息
     */
    private String address;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
