package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 驾驶员实体
 */
@Data
@TableName("driver")
public class Driver {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 驾驶证号
     */
    private String licenseNo;

    /**
     * 驾驶证类型：A1, A2, B1, B2, C1
     */
    private String licenseType;

    /**
     * 驾驶证有效期
     */
    private LocalDateTime licenseExpiry;

    /**
     * 从业资格证号
     */
    private String qualificationNo;

    /**
     * 从业资格证有效期
     */
    private LocalDateTime qualificationExpiry;

    /**
     * 驾驶员状态：AVAILABLE-空闲, BUSY-忙碌, REST-休息, OFFLINE-离线
     */
    private String status;

    /**
     * 当前车辆ID
     */
    private Long currentVehicleId;

    /**
     * 当前任务ID
     */
    private Long currentTaskId;

    /**
     * 当前位置-经度
     */
    private Double longitude;

    /**
     * 当前位置-纬度
     */
    private Double latitude;

    /**
     * 最后位置更新时间
     */
    private LocalDateTime lastLocationTime;

    /**
     * 累计行驶里程（公里）
     */
    private Double totalMileage;

    /**
     * 完成任务数
     */
    private Integer completedTasks;

    /**
     * 评分
     */
    private Double rating;

    /**
     * 入职日期
     */
    private LocalDateTime hireDate;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
