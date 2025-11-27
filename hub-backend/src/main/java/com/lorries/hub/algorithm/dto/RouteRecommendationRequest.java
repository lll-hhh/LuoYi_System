package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路线推荐请求
 */
@Data
public class RouteRecommendationRequest {
    private String vehiclePlate;
    private String vehicleType;
    private BigDecimal startLat;
    private BigDecimal startLng;
    private BigDecimal endLat;
    private BigDecimal endLng;
    private List<String> avoidRoads;
    private String preference;
}
