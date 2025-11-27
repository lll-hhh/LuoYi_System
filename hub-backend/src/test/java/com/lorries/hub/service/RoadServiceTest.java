package com.lorries.hub.service;

import com.lorries.hub.entity.Road;
import com.lorries.hub.mapper.RoadMapper;
import com.lorries.hub.service.impl.RoadServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadServiceTest {

    @Mock
    private RoadMapper roadMapper;

    @InjectMocks
    private RoadServiceImpl roadService;

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
    @DisplayName("根据ID查询道路")
    void testGetById() {
        when(roadMapper.selectById(1)).thenReturn(testRoad);

        Road result = roadService.getById(1);

        assertNotNull(result);
        assertEquals("测试道路", result.getRoadName());
        assertEquals("R001", result.getRoadCode());
        verify(roadMapper, times(1)).selectById(1);
    }

    @Test
    @DisplayName("查询所有道路")
    void testList() {
        Road road2 = new Road();
        road2.setRoadId(2);
        road2.setRoadName("测试道路2");
        road2.setRoadCode("R002");

        when(roadMapper.selectList(null)).thenReturn(Arrays.asList(testRoad, road2));

        List<Road> result = roadService.list();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roadMapper, times(1)).selectList(null);
    }

    @Test
    @DisplayName("保存道路")
    void testSave() {
        when(roadMapper.insert(any(Road.class))).thenReturn(1);

        boolean result = roadService.save(testRoad);

        assertTrue(result);
        verify(roadMapper, times(1)).insert(testRoad);
    }

    @Test
    @DisplayName("更新道路")
    void testUpdate() {
        when(roadMapper.updateById(any(Road.class))).thenReturn(1);

        boolean result = roadService.updateById(testRoad);

        assertTrue(result);
        verify(roadMapper, times(1)).updateById(testRoad);
    }

    @Test
    @DisplayName("删除道路")
    void testRemove() {
        when(roadMapper.deleteById(1)).thenReturn(1);

        boolean result = roadService.removeById(1);

        assertTrue(result);
        verify(roadMapper, times(1)).deleteById(1);
    }
}
