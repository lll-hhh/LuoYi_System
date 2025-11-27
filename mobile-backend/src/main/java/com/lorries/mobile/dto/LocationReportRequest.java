package com.lorries.mobile.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 位置上报请求
 */
@Data
public class LocationReportRequest {

    @NotNull(message = "经度不能为空")
    private Double longitude;

    @NotNull(message = "纬度不能为空")
    private Double latitude;

    private Double altitude;

    private Double speed;

    private Double direction;

    private Double accuracy;

    private String locationType;

    private String address;

    private String deviceId;

    private Long taskId;

    private LocalDateTime recordTime;
}
