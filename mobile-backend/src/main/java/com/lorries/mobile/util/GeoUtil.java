package com.lorries.mobile.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 地理位置工具类
 */
public class GeoUtil {

    private static final double EARTH_RADIUS = 6371000; // 地球半径，单位：米

    /**
     * 计算两点之间的距离（Haversine公式）
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 计算两点之间的距离（返回格式化的字符串）
     */
    public static String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0f米", meters);
        } else {
            return String.format("%.1f公里", meters / 1000);
        }
    }

    /**
     * 计算方位角
     */
    public static double calculateBearing(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double y = Math.sin(dLng) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    /**
     * 根据方位角获取方向描述
     */
    public static String getDirection(double bearing) {
        String[] directions = {"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
        int index = (int) Math.round(bearing / 45) % 8;
        return directions[index];
    }

    /**
     * 判断点是否在圆形区域内
     */
    public static boolean isPointInCircle(double pointLat, double pointLng, 
                                          double centerLat, double centerLng, double radius) {
        double distance = calculateDistance(pointLat, pointLng, centerLat, centerLng);
        return distance <= radius;
    }

    /**
     * 判断点是否在矩形区域内
     */
    public static boolean isPointInRectangle(double pointLat, double pointLng,
                                             double minLat, double minLng, 
                                             double maxLat, double maxLng) {
        return pointLat >= minLat && pointLat <= maxLat && 
               pointLng >= minLng && pointLng <= maxLng;
    }

    /**
     * 计算预计到达时间（秒）
     */
    public static long calculateETA(double distanceMeters, double speedKmh) {
        if (speedKmh <= 0) {
            return -1;
        }
        double speedMps = speedKmh * 1000 / 3600;
        return Math.round(distanceMeters / speedMps);
    }

    /**
     * 格式化预计到达时间
     */
    public static String formatETA(long seconds) {
        if (seconds < 0) {
            return "未知";
        }
        if (seconds < 60) {
            return "即将到达";
        } else if (seconds < 3600) {
            return String.format("%d分钟", seconds / 60);
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return String.format("%d小时%d分钟", hours, minutes);
        }
    }

    /**
     * 经纬度精度处理
     */
    public static double roundCoordinate(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
