package com.lorries.mobile.service;

import com.lorries.mobile.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vehicle 实体和服务单元测试
 */
class VehicleServiceTest {

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle();
        testVehicle.setVehicleId(1);
        testVehicle.setPlateNumber("京A12345");
        testVehicle.setVehicleTypeId(1);
        testVehicle.setBrand("东风");
        testVehicle.setStatus("normal");
    }

    @Test
    @DisplayName("Vehicle实体 - 属性设置和获取")
    void testVehicleProperties() {
        assertEquals(1, testVehicle.getVehicleId());
        assertEquals("京A12345", testVehicle.getPlateNumber());
        assertEquals(1, testVehicle.getVehicleTypeId());
        assertEquals("东风", testVehicle.getBrand());
        assertEquals("normal", testVehicle.getStatus());
    }

    @Test
    @DisplayName("Vehicle实体 - 属性修改")
    void testVehiclePropertyModification() {
        testVehicle.setPlateNumber("京B67890");
        testVehicle.setBrand("解放");
        
        assertEquals("京B67890", testVehicle.getPlateNumber());
        assertEquals("解放", testVehicle.getBrand());
    }

    @Test
    @DisplayName("Vehicle实体 - 新对象创建")
    void testNewVehicle() {
        Vehicle newVehicle = new Vehicle();
        newVehicle.setVehicleId(2);
        newVehicle.setPlateNumber("沪C11111");
        
        assertNotNull(newVehicle);
        assertEquals(2, newVehicle.getVehicleId());
        assertEquals("沪C11111", newVehicle.getPlateNumber());
    }

    @Test
    @DisplayName("VehicleService - 存在性检查")
    void testVehicleServiceExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.mobile.service.VehicleService");
            assertNotNull(clazz);
        } catch (ClassNotFoundException e) {
            fail("VehicleService class should exist");
        }
    }
}
