package com.lorries.mobile.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务响应VO
 */
@Data
public class TaskVO {

    private Long id;
    private String taskNo;
    private String taskType;
    private String taskTypeName;
    private String status;
    private String statusName;
    private String priority;
    private String priorityName;

    private Long vehicleId;
    private String vehiclePlate;
    private Long driverId;
    private String driverName;
    private Long cargoId;
    private String cargoName;

    private String startAddress;
    private Double startLongitude;
    private Double startLatitude;
    private String endAddress;
    private Double endLongitude;
    private Double endLatitude;

    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    private Double estimatedDistance;
    private Double actualDistance;
    private String description;
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
