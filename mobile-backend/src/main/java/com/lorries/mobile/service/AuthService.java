package com.lorries.mobile.service;

import com.lorries.mobile.dto.LoginRequest;
import com.lorries.mobile.dto.LoginResponse;
import com.lorries.mobile.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void sendVerifyCode(String phone);

    LoginResponse refreshToken(String refreshToken);
}
