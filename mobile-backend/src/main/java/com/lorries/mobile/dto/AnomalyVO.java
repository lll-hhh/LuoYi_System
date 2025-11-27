package com.lorries.mobile.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 异常事件VO
 */
@Data
public class AnomalyVO {

    private Long id;
    private String eventNo;
    private String eventType;
    private String eventTypeName;
    private String severity;
    private String severityName;
    private String status;
    private String statusName;

    private Long taskId;
    private String taskNo;
    private Long vehicleId;
    private String vehiclePlate;
    private Long driverId;
    private String driverName;

    private String title;
    private String description;
    private String location;
    private Double longitude;
    private Double latitude;

    private String[] images;
    private String videoUrl;

    private Long reportedBy;
    private String reportedByName;
    private LocalDateTime reportedAt;

    private Long handledBy;
    private String handledByName;
    private LocalDateTime handledAt;
    private String resolution;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
