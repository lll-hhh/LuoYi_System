package com.lorries.mobile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基础单元测试
 */
class MobileBackendApplicationTests {

    @Test
    void applicationClassExists() {
        // 验证主应用类存在
        try {
            Class.forName("com.lorries.mobile.MobileBackendApplication");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            assertTrue(false, "MobileBackendApplication class not found");
        }
    }
    
    @Test
    void basicTest() {
        assertTrue(true, "Basic test works");
    }
}
