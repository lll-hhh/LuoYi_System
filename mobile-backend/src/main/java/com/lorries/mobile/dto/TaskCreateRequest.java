package com.lorries.mobile.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建任务请求
 */
@Data
public class TaskCreateRequest {

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    private String priority = "MEDIUM";

    private Long vehicleId;

    private Long driverId;

    private Long cargoId;

    @NotBlank(message = "起始地址不能为空")
    private String startAddress;

    @NotNull(message = "起始经度不能为空")
    private Double startLongitude;

    @NotNull(message = "起始纬度不能为空")
    private Double startLatitude;

    @NotBlank(message = "目的地址不能为空")
    private String endAddress;

    @NotNull(message = "目的经度不能为空")
    private Double endLongitude;

    @NotNull(message = "目的纬度不能为空")
    private Double endLatitude;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    private String description;

    private String remark;
}
