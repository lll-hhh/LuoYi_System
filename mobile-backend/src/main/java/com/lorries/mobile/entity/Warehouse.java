package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仓库实体
 */
@Data
@TableName("warehouse")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 仓库编码
     */
    private String code;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 仓库类型：TRANSIT-中转仓, STORAGE-存储仓, DISTRIBUTION-配送仓
     */
    private String warehouseType;

    /**
     * 仓库状态：ACTIVE-正常, INACTIVE-停用, MAINTENANCE-维护中
     */
    private String status;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 面积（平方米）
     */
    private Double area;

    /**
     * 总容量
     */
    private Integer capacity;

    /**
     * 已用容量
     */
    private Integer usedCapacity;

    /**
     * 管理员姓名
     */
    private String manager;

    /**
     * 管理员电话
     */
    private String managerPhone;

    /**
     * 营业时间
     */
    private String businessHours;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
