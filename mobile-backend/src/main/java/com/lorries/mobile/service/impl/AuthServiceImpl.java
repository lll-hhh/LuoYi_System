package com.lorries.mobile.service.impl;

import com.lorries.mobile.common.constant.Constants;
import com.lorries.mobile.dto.LoginRequest;
import com.lorries.mobile.dto.LoginResponse;
import com.lorries.mobile.dto.RegisterRequest;
import com.lorries.mobile.entity.User;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.exception.UnauthorizedException;
import com.lorries.mobile.mapper.UserMapper;
import com.lorries.mobile.service.AuthService;
import com.lorries.mobile.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.expiration:604800000}")
    private long accessTokenTtl;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshTokenTtl;

    private static final DateTimeFormatter OTP_LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getPhone())) {
            throw new BusinessException("手机号不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        validateVerifyCode(request.getPhone(), request.getVerifyCode());

        User exists = userMapper.findByPhone(request.getPhone());
        if (exists != null) {
            throw new BusinessException("手机号已注册");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setUsername(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(Constants.USER_STATUS_ACTIVE);
        user.setLoginCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        cleanupVerifyCode(request.getPhone());
        log.info("[AUTH] 用户 {} 注册成功", request.getPhone());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.getPhone()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("手机号或密码不能为空");
        }

        User user = userMapper.findByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }
        if (!Constants.USER_STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(Optional.ofNullable(user.getLoginCount()).orElse(0) + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return buildLoginResponse(user);
    }

    @Override
    public void sendVerifyCode(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        String key = Constants.REDIS_KEY_VERIFY_CODE + phone;
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        log.info("[AUTH] 发送验证码 {} 至 {} (仅限调试，{} )", code, phone, LocalDateTime.now().format(OTP_LOG_TIME));
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new UnauthorizedException("刷新Token不能为空");
        }
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("刷新Token无效或已过期");
        }
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new UnauthorizedException("令牌类型错误");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new UnauthorizedException("无法解析刷新Token");
        }

        User user = userMapper.selectById(userId.intValue());
        if (user == null) {
            throw new ResourceNotFoundException("用户", userId);
        }
        if (!Constants.USER_STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getUserId().longValue(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId().longValue(), user.getPhone());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getUserId(),
                user.getPhone(),
                user.getRealName(),
                user.getAvatarUrl(),
                user.getCompanyName()
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                Constants.TOKEN_PREFIX.trim(),
                accessTokenTtl / 1000,
                userInfo
        );
    }

    private void validateVerifyCode(String phone, String verifyCode) {
        if (!StringUtils.hasText(verifyCode)) {
            throw new BusinessException("验证码不能为空");
        }
        String cacheKey = Constants.REDIS_KEY_VERIFY_CODE + phone;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (!verifyCode.equals(cached)) {
            throw new BusinessException("验证码错误或已过期");
        }
    }

    private void cleanupVerifyCode(String phone) {
        stringRedisTemplate.delete(Constants.REDIS_KEY_VERIFY_CODE + phone);
    }
}
