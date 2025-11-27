package com.lorries.hub.controller;

import com.lorries.hub.entity.Road;
import com.lorries.hub.service.RoadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoadService roadService;

    @Test
    @DisplayName("获取道路列表 - 未认证访问")
    void listRoadsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取道路列表 - 已认证访问")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listRoadsWithAuth() throws Exception {
        Road road1 = new Road();
        road1.setRoadId(1);
        road1.setRoadName("测试道路1");
        road1.setRoadCode("R001");

        Road road2 = new Road();
        road2.setRoadId(2);
        road2.setRoadName("测试道路2");
        road2.setRoadCode("R002");

        List<Road> roads = Arrays.asList(road1, road2);
        when(roadService.list()).thenReturn(roads);

        mockMvc.perform(get("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
