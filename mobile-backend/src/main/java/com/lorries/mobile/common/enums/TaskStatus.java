package com.lorries.mobile.common.enums;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    
    PENDING("PENDING", "待分配"),
    ASSIGNED("ASSIGNED", "已分配"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String name;
    
    TaskStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
