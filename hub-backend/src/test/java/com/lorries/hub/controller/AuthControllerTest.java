package com.lorries.hub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorries.hub.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthController 单元测试
 */
class AuthControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("LoginRequest - 序列化测试")
    void testLoginRequestSerialization() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("admin"));
        assertTrue(json.contains("admin123"));
    }

    @Test
    @DisplayName("LoginRequest - 反序列化测试")
    void testLoginRequestDeserialization() throws Exception {
        String json = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);
        
        assertEquals("testuser", request.getUsername());
        assertEquals("testpass", request.getPassword());
    }
    
    @Test
    @DisplayName("AuthController - 类存在测试")
    void testAuthControllerExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.hub.controller.AuthController");
            assertNotNull(clazz);
        } catch (ClassNotFoundException e) {
            fail("AuthController class should exist");
        }
    }
}
