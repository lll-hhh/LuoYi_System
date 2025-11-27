package com.lorries.mobile.common.enums;

/**
 * 异常事件类型枚举
 */
public enum AnomalyType {
    
    ACCIDENT("ACCIDENT", "事故"),
    DELAY("DELAY", "延误"),
    DAMAGE("DAMAGE", "货损"),
    VIOLATION("VIOLATION", "违章"),
    EQUIPMENT("EQUIPMENT", "设备故障"),
    WEATHER("WEATHER", "恶劣天气"),
    TRAFFIC("TRAFFIC", "交通拥堵"),
    OTHER("OTHER", "其他");
    
    private final String code;
    private final String name;
    
    AnomalyType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static AnomalyType fromCode(String code) {
        for (AnomalyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
