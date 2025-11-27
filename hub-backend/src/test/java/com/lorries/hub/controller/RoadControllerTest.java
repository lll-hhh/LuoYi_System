package com.lorries.hub.controller;

import com.lorries.hub.entity.Road;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoadController 单元测试
 */
class RoadControllerTest {

    @Test
    @DisplayName("Road实体 - 基本属性测试")
    void testRoadEntity() {
        Road road = new Road();
        road.setRoadId(1);
        road.setRoadName("测试道路");
        road.setRoadCode("R001");

        assertEquals(1, road.getRoadId());
        assertEquals("测试道路", road.getRoadName());
        assertEquals("R001", road.getRoadCode());
    }

    @Test
    @DisplayName("Road实体 - 空对象测试")
    void testRoadEntityEmpty() {
        Road road = new Road();
        assertNull(road.getRoadName());
        assertNull(road.getRoadCode());
    }

    @Test
    @DisplayName("RoadController - 类存在测试")
    void testRoadControllerExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.hub.controller.RoadController");
            assertNotNull(clazz);
        } catch (ClassNotFoundException e) {
            fail("RoadController class should exist");
        }
    }
}
