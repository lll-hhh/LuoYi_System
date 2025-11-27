package com.lorries.mobile.common.enums;

/**
 * 货物类型枚举
 */
public enum CargoType {
    
    GENERAL("GENERAL", "普通货物"),
    FRAGILE("FRAGILE", "易碎品"),
    PERISHABLE("PERISHABLE", "易腐品"),
    DANGEROUS("DANGEROUS", "危险品"),
    VALUABLE("VALUABLE", "贵重物品"),
    OVERSIZED("OVERSIZED", "超大件"),
    LIQUID("LIQUID", "液体"),
    FROZEN("FROZEN", "冷冻品");
    
    private final String code;
    private final String name;
    
    CargoType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static CargoType fromCode(String code) {
        for (CargoType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return GENERAL;
    }
}
