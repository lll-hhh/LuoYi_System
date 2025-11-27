package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 异常检测请求
 */
@Data
public class AnomalyDetectionRequest {
    private Integer cameraId;
    private String imageBase64;
    private String imageUrl;
    private String videoUrl;
    private String plateNumber;
    private BigDecimal speed;
}
