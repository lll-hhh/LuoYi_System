package com.lorries.mobile.service;

import com.lorries.mobile.entity.Vehicle;
import com.lorries.mobile.mapper.VehicleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle();
        testVehicle.setVehicleId(1);
        testVehicle.setPlateNumber("京A12345");
        testVehicle.setVehicleType("货车");
        testVehicle.setBrand("东风");
        testVehicle.setStatus("normal");
    }

    @Test
    @DisplayName("根据用户ID查询车辆列表")
    void testFindByUserId() {
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVehicleId(2);
        vehicle2.setPlateNumber("京B67890");

        when(vehicleMapper.selectByUserId(1L)).thenReturn(Arrays.asList(testVehicle, vehicle2));

        List<Vehicle> result = vehicleService.findByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(vehicleMapper, times(1)).selectByUserId(1L);
    }

    @Test
    @DisplayName("根据车牌号查询车辆")
    void testFindByPlateNumber() {
        when(vehicleMapper.selectByPlateNumber("京A12345")).thenReturn(testVehicle);

        Vehicle result = vehicleService.findByPlateNumber("京A12345");

        assertNotNull(result);
        assertEquals("京A12345", result.getPlateNumber());
        verify(vehicleMapper, times(1)).selectByPlateNumber("京A12345");
    }

    @Test
    @DisplayName("添加车辆")
    void testAddVehicle() {
        when(vehicleMapper.insert(any(Vehicle.class))).thenReturn(1);

        boolean result = vehicleService.addVehicle(testVehicle);

        assertTrue(result);
        verify(vehicleMapper, times(1)).insert(testVehicle);
    }

    @Test
    @DisplayName("更新车辆信息")
    void testUpdateVehicle() {
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        boolean result = vehicleService.updateVehicle(testVehicle);

        assertTrue(result);
        verify(vehicleMapper, times(1)).updateById(testVehicle);
    }
}
