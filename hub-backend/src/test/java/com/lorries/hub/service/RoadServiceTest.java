package com.lorries.hub.service;

import com.lorries.hub.entity.Road;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Road 实体和服务单元测试
 */
class RoadServiceTest {

    private Road testRoad;

    @BeforeEach
    void setUp() {
        testRoad = new Road();
        testRoad.setRoadId(1);
        testRoad.setRoadName("测试道路");
        testRoad.setRoadCode("R001");
        testRoad.setRoadLevel("主干道");
        testRoad.setLaneCount(4);
        testRoad.setStatus("normal");
    }

    @Test
    @DisplayName("Road实体 - 属性设置和获取")
    void testRoadProperties() {
        assertEquals(1, testRoad.getRoadId());
        assertEquals("测试道路", testRoad.getRoadName());
        assertEquals("R001", testRoad.getRoadCode());
        assertEquals("主干道", testRoad.getRoadLevel());
        assertEquals(4, testRoad.getLaneCount());
        assertEquals("normal", testRoad.getStatus());
    }

    @Test
    @DisplayName("Road实体 - 属性修改")
    void testRoadPropertyModification() {
        testRoad.setRoadName("新道路名称");
        testRoad.setLaneCount(6);
        
        assertEquals("新道路名称", testRoad.getRoadName());
        assertEquals(6, testRoad.getLaneCount());
    }

    @Test
    @DisplayName("Road实体 - 新对象创建")
    void testNewRoad() {
        Road newRoad = new Road();
        newRoad.setRoadId(2);
        newRoad.setRoadName("新建道路");
        newRoad.setRoadCode("R002");
        
        assertNotNull(newRoad);
        assertEquals(2, newRoad.getRoadId());
        assertEquals("新建道路", newRoad.getRoadName());
    }

    @Test
    @DisplayName("RoadService接口 - 存在性检查")
    void testRoadServiceExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.hub.service.RoadService");
            assertNotNull(clazz);
            assertTrue(clazz.isInterface());
        } catch (ClassNotFoundException e) {
            fail("RoadService interface should exist");
        }
    }

    @Test
    @DisplayName("RoadServiceImpl - 存在性检查")
    void testRoadServiceImplExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.hub.service.impl.RoadServiceImpl");
            assertNotNull(clazz);
        } catch (ClassNotFoundException e) {
            fail("RoadServiceImpl class should exist");
        }
    }
}
