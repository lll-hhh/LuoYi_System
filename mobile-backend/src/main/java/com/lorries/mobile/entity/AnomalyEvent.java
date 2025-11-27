package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 异常事件实体
 */
@Data
@TableName("anomaly_event")
public class AnomalyEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件编号
     */
    private String eventNo;

    /**
     * 事件类型：ACCIDENT-事故, DELAY-延误, DAMAGE-货损, VIOLATION-违章, EQUIPMENT-设备故障
     */
    private String eventType;

    /**
     * 严重级别：LOW, MEDIUM, HIGH, CRITICAL
     */
    private String severity;

    /**
     * 事件状态：PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, CLOSED-已关闭
     */
    private String status;

    /**
     * 关联任务ID
     */
    private Long taskId;

    /**
     * 关联车辆ID
     */
    private Long vehicleId;

    /**
     * 关联司机ID
     */
    private Long driverId;

    /**
     * 事件标题
     */
    private String title;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 发生地点
     */
    private String location;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 图片URL列表（JSON）
     */
    private String images;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 上报人ID
     */
    private Long reportedBy;

    /**
     * 上报时间
     */
    private LocalDateTime reportedAt;

    /**
     * 处理人ID
     */
    private Long handledBy;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 处理结果
     */
    private String resolution;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
