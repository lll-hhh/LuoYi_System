package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆识别响应
 */
@Data
public class VehicleRecognitionResponse {
    private Boolean success;
    private String plateNumber;
    private String plateColor;
    private String vehicleType;
    private String vehicleColor;
    private String vehicleBrand;
    private BigDecimal confidence;
    private BoundingBox boundingBox;
    private LocalDateTime processTime;
    private String errorMessage;

    @Data
    public static class BoundingBox {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }
}
