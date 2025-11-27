package com.lorries.hub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", 
            "testSecretKeyForJwtTokenTestingPurposeOnly12345678901234567890");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);
    }

    @Test
    @DisplayName("生成Token")
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT格式: header.payload.signature
    }

    @Test
    @DisplayName("从Token获取用户名")
    void testGetUsernameFromToken() {
        String token = jwtTokenProvider.generateToken("testuser");
        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("验证有效Token")
    void testValidateToken() {
        String token = jwtTokenProvider.generateToken("testuser");
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证无效Token")
    void testValidateInvalidToken() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证空Token")
    void testValidateEmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证null Token")
    void testValidateNullToken() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }
}
