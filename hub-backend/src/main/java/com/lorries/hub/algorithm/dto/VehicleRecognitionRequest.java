package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆识别请求
 */
@Data
public class VehicleRecognitionRequest {
    private Integer cameraId;
    private String imageBase64;
    private String imageUrl;
    private LocalDateTime captureTime;
}
