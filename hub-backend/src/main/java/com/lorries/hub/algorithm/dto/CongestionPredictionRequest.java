package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拥堵预测请求
 */
@Data
public class CongestionPredictionRequest {
    private Integer roadId;
    private Integer junctionId;
    private LocalDateTime targetTime;
    private Integer predictionHours;
}
