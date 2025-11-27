package com.lorries.mobile.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物信息VO
 */
@Data
public class CargoVO {

    private Long id;
    private String trackingNo;
    private String name;
    private String cargoType;
    private String cargoTypeName;
    private String status;
    private String statusName;

    private BigDecimal weight;
    private BigDecimal volume;
    private Integer quantity;
    private BigDecimal value;

    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private Long warehouseId;
    private String warehouseName;
    private String currentLocation;

    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;
    private String signedBy;
    private LocalDateTime signedAt;

    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
