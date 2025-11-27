package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 运输任务实体
 */
@Data
@TableName("transport_task")
public class TransportTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 任务类型：DELIVERY-配送, PICKUP-取货, TRANSFER-调拨
     */
    private String taskType;

    /**
     * 任务状态：PENDING-待分配, ASSIGNED-已分配, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消
     */
    private String status;

    /**
     * 优先级：LOW, MEDIUM, HIGH, URGENT
     */
    private String priority;

    /**
     * 关联车辆ID
     */
    private Long vehicleId;

    /**
     * 关联司机ID
     */
    private Long driverId;

    /**
     * 关联货物ID
     */
    private Long cargoId;

    /**
     * 起始地址
     */
    private String startAddress;

    /**
     * 起始经度
     */
    private Double startLongitude;

    /**
     * 起始纬度
     */
    private Double startLatitude;

    /**
     * 目的地址
     */
    private String endAddress;

    /**
     * 目的经度
     */
    private Double endLongitude;

    /**
     * 目的纬度
     */
    private Double endLatitude;

    /**
     * 计划出发时间
     */
    private LocalDateTime plannedStartTime;

    /**
     * 计划到达时间
     */
    private LocalDateTime plannedEndTime;

    /**
     * 实际出发时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际到达时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 预计行驶里程（公里）
     */
    private Double estimatedDistance;

    /**
     * 实际行驶里程（公里）
     */
    private Double actualDistance;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
