package com.lorries.mobile.common.constant;

/**
 * 系统常量
 */
public class Constants {
    
    // 认证相关
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 7天
    
    // 用户状态
    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_DISABLED = "DISABLED";
    
    // 司机状态
    public static final String DRIVER_STATUS_AVAILABLE = "AVAILABLE";
    public static final String DRIVER_STATUS_BUSY = "BUSY";
    public static final String DRIVER_STATUS_OFFLINE = "OFFLINE";
    
    // 车辆状态
    public static final String VEHICLE_STATUS_RUNNING = "RUNNING";
    public static final String VEHICLE_STATUS_IDLE = "IDLE";
    public static final String VEHICLE_STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String VEHICLE_STATUS_OFFLINE = "OFFLINE";
    
    // 任务状态
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_ASSIGNED = "ASSIGNED";
    public static final String TASK_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String TASK_STATUS_COMPLETED = "COMPLETED";
    public static final String TASK_STATUS_CANCELLED = "CANCELLED";
    
    // 货物状态
    public static final String CARGO_STATUS_PENDING = "PENDING";
    public static final String CARGO_STATUS_TRANSIT = "TRANSIT";
    public static final String CARGO_STATUS_ARRIVED = "ARRIVED";
    public static final String CARGO_STATUS_DELIVERED = "DELIVERED";
    
    // 异常状态
    public static final String ANOMALY_STATUS_PENDING = "PENDING";
    public static final String ANOMALY_STATUS_PROCESSING = "PROCESSING";
    public static final String ANOMALY_STATUS_RESOLVED = "RESOLVED";
    public static final String ANOMALY_STATUS_CLOSED = "CLOSED";
    
    // 严重程度
    public static final String SEVERITY_LOW = "LOW";
    public static final String SEVERITY_MEDIUM = "MEDIUM";
    public static final String SEVERITY_HIGH = "HIGH";
    public static final String SEVERITY_CRITICAL = "CRITICAL";
    
    // 优先级
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";
    
    // 仓库状态
    public static final String WAREHOUSE_STATUS_ACTIVE = "ACTIVE";
    public static final String WAREHOUSE_STATUS_INACTIVE = "INACTIVE";
    
    // Redis Key前缀
    public static final String REDIS_KEY_TOKEN = "token:";
    public static final String REDIS_KEY_USER = "user:";
    public static final String REDIS_KEY_DRIVER_LOCATION = "driver:location:";
    public static final String REDIS_KEY_VEHICLE_LOCATION = "vehicle:location:";
    public static final String REDIS_KEY_VERIFY_CODE = "verify:code:";
    
    // 分页默认值
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    
    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
