package com.lorries.hub.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtTokenProvider 单元测试
 */
class JwtTokenProviderTest {

    @Test
    @DisplayName("JwtTokenProvider - 类存在测试")
    void testJwtTokenProviderExists() {
        try {
            Class<?> clazz = Class.forName("com.lorries.hub.security.JwtTokenProvider");
            assertNotNull(clazz);
        } catch (ClassNotFoundException e) {
            fail("JwtTokenProvider class should exist");
        }
    }

    @Test
    @DisplayName("JwtTokenProvider - 包含generateToken方法")
    void testHasGenerateTokenMethod() throws Exception {
        Class<?> clazz = Class.forName("com.lorries.hub.security.JwtTokenProvider");
        boolean hasMethod = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("generateToken")) {
                hasMethod = true;
                break;
            }
        }
        assertTrue(hasMethod, "Should have generateToken method");
    }

    @Test
    @DisplayName("JwtTokenProvider - 包含validateToken方法")
    void testHasValidateTokenMethod() throws Exception {
        Class<?> clazz = Class.forName("com.lorries.hub.security.JwtTokenProvider");
        boolean hasMethod = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("validateToken")) {
                hasMethod = true;
                break;
            }
        }
        assertTrue(hasMethod, "Should have validateToken method");
    }

    @Test
    @DisplayName("JwtTokenProvider - 包含getUsernameFromToken方法")
    void testHasGetUsernameFromTokenMethod() throws Exception {
        Class<?> clazz = Class.forName("com.lorries.hub.security.JwtTokenProvider");
        boolean hasMethod = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("getUsernameFromToken")) {
                hasMethod = true;
                break;
            }
        }
        assertTrue(hasMethod, "Should have getUsernameFromToken method");
    }

    @Test
    @DisplayName("JwtTokenProvider - 是Spring组件")
    void testIsSpringComponent() throws Exception {
        Class<?> clazz = Class.forName("com.lorries.hub.security.JwtTokenProvider");
        boolean hasComponentAnnotation = clazz.isAnnotationPresent(
            org.springframework.stereotype.Component.class
        );
        assertTrue(hasComponentAnnotation, "Should be annotated with @Component");
    }
}
