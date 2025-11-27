package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拥堵预测响应
 */
@Data
public class CongestionPredictionResponse {
    private Boolean success;
    private List<PredictionResult> predictions;
    private String errorMessage;

    @Data
    public static class PredictionResult {
        private LocalDateTime targetTime;
        private BigDecimal predictedIndex;
        private String predictedLevel;
        private BigDecimal confidence;
        private String suggestion;
    }
}
