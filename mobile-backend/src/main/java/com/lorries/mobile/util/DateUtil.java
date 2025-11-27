package com.lorries.mobile.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_PATTERN);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(Date date) {
        return format(date, DEFAULT_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return format(dateTime, pattern);
    }

    /**
     * 解析日期时间
     */
    public static LocalDateTime parse(String dateStr) {
        return parse(dateStr, DEFAULT_PATTERN);
    }

    public static LocalDateTime parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取今天开始时间
     */
    public static LocalDateTime todayStart() {
        return LocalDateTime.now().toLocalDate().atStartOfDay();
    }

    /**
     * 获取今天结束时间
     */
    public static LocalDateTime todayEnd() {
        return LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
    }

    /**
     * 计算两个时间的差值（秒）
     */
    public static long diffSeconds(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).getSeconds();
    }

    /**
     * 计算两个时间的差值（分钟）
     */
    public static long diffMinutes(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toMinutes();
    }

    /**
     * 计算两个时间的差值（小时）
     */
    public static long diffHours(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toHours();
    }

    /**
     * 格式化时长
     */
    public static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        } else {
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            return days + "天" + (hours > 0 ? hours + "小时" : "");
        }
    }

    /**
     * 获取相对时间描述
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知";
        }
        
        long seconds = diffSeconds(dateTime, LocalDateTime.now());
        
        if (seconds < 0) {
            return "未来";
        } else if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时前";
        } else if (seconds < 2592000) {
            return (seconds / 86400) + "天前";
        } else {
            return format(dateTime, DATE_PATTERN);
        }
    }
}
