package com.lorries.hub.algorithm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路线推荐响应
 */
@Data
public class RouteRecommendationResponse {
    private Boolean success;
    private RouteInfo recommendedRoute;
    private List<RouteInfo> alternativeRoutes;
    private String errorMessage;

    @Data
    public static class RouteInfo {
        private Integer routeId;
        private String routeName;
        private List<Integer> roadIds;
        private BigDecimal totalDistance;
        private Integer estimatedTime;
        private BigDecimal congestionIndex;
        private List<WayPoint> wayPoints;
    }

    @Data
    public static class WayPoint {
        private BigDecimal lat;
        private BigDecimal lng;
        private String description;
    }
}
