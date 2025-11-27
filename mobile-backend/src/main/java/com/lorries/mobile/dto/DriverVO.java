package com.lorries.mobile.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 司机信息VO
 */
@Data
public class DriverVO {

    private Long id;
    private Long userId;
    private String employeeNo;
    private String name;
    private String avatar;
    private String phone;
    private String licenseNo;
    private String licenseType;
    private LocalDateTime licenseExpiry;
    private String qualificationNo;
    private LocalDateTime qualificationExpiry;
    private String status;
    private String statusName;

    private Long currentVehicleId;
    private String currentVehiclePlate;
    private Long currentTaskId;
    private String currentTaskNo;

    private Double longitude;
    private Double latitude;
    private LocalDateTime lastLocationTime;

    private Double totalMileage;
    private Integer completedTasks;
    private Double rating;

    private LocalDateTime hireDate;
    private String emergencyContact;
    private String emergencyPhone;
}
