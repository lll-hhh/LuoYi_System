package com.lorries.hub;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基础单元测试
 */
class HubBackendApplicationTests {

    @Test
    void applicationClassExists() {
        // 验证主应用类存在
        try {
            Class.forName("com.lorries.hub.HubBackendApplication");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            assertTrue(false, "HubBackendApplication class not found");
        }
    }
    
    @Test
    void basicAssertionWorks() {
        assertTrue(1 + 1 == 2, "Basic math works");
    }
}
