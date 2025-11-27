package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物实体
 */
@Data
@TableName("cargo")
public class Cargo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 货物编号/追踪号
     */
    private String trackingNo;

    /**
     * 货物名称
     */
    private String name;

    /**
     * 货物类型：GENERAL-普通, FRAGILE-易碎, PERISHABLE-易腐, DANGEROUS-危险品, VALUABLE-贵重
     */
    private String cargoType;

    /**
     * 货物状态：PENDING-待发, TRANSIT-运输中, ARRIVED-已到达, DELIVERED-已签收
     */
    private String status;

    /**
     * 重量（kg）
     */
    private BigDecimal weight;

    /**
     * 体积（立方米）
     */
    private BigDecimal volume;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 货值（元）
     */
    private BigDecimal value;

    /**
     * 发货人名称
     */
    private String senderName;

    /**
     * 发货人电话
     */
    private String senderPhone;

    /**
     * 发货地址
     */
    private String senderAddress;

    /**
     * 收货人名称
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 当前仓库ID
     */
    private Long warehouseId;

    /**
     * 当前位置描述
     */
    private String currentLocation;

    /**
     * 预计到达时间
     */
    private LocalDateTime estimatedArrival;

    /**
     * 实际到达时间
     */
    private LocalDateTime actualArrival;

    /**
     * 签收人
     */
    private String signedBy;

    /**
     * 签收时间
     */
    private LocalDateTime signedAt;

    /**
     * 签收图片
     */
    private String signatureImage;

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
