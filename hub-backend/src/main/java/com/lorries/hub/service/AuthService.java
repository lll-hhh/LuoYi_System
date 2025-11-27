package com.lorries.hub.service;

import com.lorries.hub.dto.LoginRequest;
import com.lorries.hub.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 修改密码
     */
    void changePassword(String username, String oldPassword, String newPassword);

    /**
     * 登出
     */
    void logout(String token);
}
