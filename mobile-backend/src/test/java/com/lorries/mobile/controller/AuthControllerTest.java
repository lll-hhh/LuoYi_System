package com.lorries.mobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorries.mobile.dto.LoginRequest;
import com.lorries.mobile.dto.RegisterRequest;
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
        request.setPhone("13800138000");
        request.setPassword("password123");

        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("13800138000"));
    }

    @Test
    @DisplayName("RegisterRequest - 序列化测试")
    void testRegisterRequestSerialization() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138001");
        request.setPassword("password123");
        request.setRealName("测试用户");
        request.setVerifyCode("123456");

        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("13800138001"));
    }

    @Test
    @DisplayName("AuthController - 类存在测试")
    void testAuthControllerExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.mobile.controller.AuthController");
            assertNotNull(clazz);
        } catch (ClassNotFoundException e) {
            fail("AuthController class should exist");
        }
    }
}
