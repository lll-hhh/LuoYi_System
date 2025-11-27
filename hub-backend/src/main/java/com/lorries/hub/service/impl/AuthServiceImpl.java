package com.lorries.hub.service.impl;

import com.lorries.hub.common.exception.BusinessException;
import com.lorries.hub.dto.LoginRequest;
import com.lorries.hub.dto.LoginResponse;
import com.lorries.hub.entity.Employee;
import com.lorries.hub.mapper.EmployeeMapper;
import com.lorries.hub.security.JwtTokenProvider;
import com.lorries.hub.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    public LoginResponse login(LoginRequest request) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 生成Token
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getUsername());

        // 获取员工信息
        Employee employee = employeeMapper.findByUsername(request.getUsername());
        
        // 更新登录信息
        employee.setLastLoginAt(LocalDateTime.now());
        employee.setLoginCount(employee.getLoginCount() + 1);
        employeeMapper.updateById(employee);

        // 构建响应
        LoginResponse.EmployeeInfo employeeInfo = new LoginResponse.EmployeeInfo(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getRealName(),
                employee.getAvatar(),
                employee.getRoleName(),
                employee.getRoleCode(),
                employee.getDepartmentName()
        );

        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtExpiration, employeeInfo);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(401, "刷新Token无效或已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Employee employee = employeeMapper.findByUsername(username);
        
        if (employee == null || !"ACTIVE".equals(employee.getStatus())) {
            throw new BusinessException(401, "用户不存在或已被禁用");
        }

        String newAccessToken = jwtTokenProvider.generateToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        LoginResponse.EmployeeInfo employeeInfo = new LoginResponse.EmployeeInfo(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getRealName(),
                employee.getAvatar(),
                employee.getRoleName(),
                employee.getRoleCode(),
                employee.getDepartmentName()
        );

        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer", jwtExpiration, employeeInfo);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Employee employee = employeeMapper.findByUsername(username);
        if (employee == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeMapper.updateById(employee);
        log.info("用户 {} 修改密码成功", username);
    }

    @Override
    public void logout(String token) {
        // 将Token加入黑名单
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (jwtTokenProvider.validateToken(token)) {
            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", jwtExpiration, TimeUnit.MILLISECONDS);
            log.info("Token已加入黑名单");
        }
    }
}
