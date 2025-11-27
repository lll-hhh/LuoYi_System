package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 异常检测响应
 */
@Data
public class AnomalyDetectionResponse {
    private Boolean success;
    private Boolean hasAnomaly;
    private List<AnomalyInfo> anomalies;
    private String errorMessage;

    @Data
    public static class AnomalyInfo {
        private String anomalyType;
        private String severity;
        private String description;
        private BigDecimal confidence;
        private String snapshotUrl;
    }
}
