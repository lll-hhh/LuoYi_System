package com.lorries.mobile.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 异常事件上报请求
 */
@Data
public class AnomalyReportRequest {

    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    @NotBlank(message = "严重级别不能为空")
    private String severity;

    private Long taskId;

    private Long vehicleId;

    @NotBlank(message = "事件标题不能为空")
    private String title;

    @NotBlank(message = "事件描述不能为空")
    private String description;

    private String location;

    @NotNull(message = "经度不能为空")
    private Double longitude;

    @NotNull(message = "纬度不能为空")
    private Double latitude;

    private List<String> images;

    private String videoUrl;
}
