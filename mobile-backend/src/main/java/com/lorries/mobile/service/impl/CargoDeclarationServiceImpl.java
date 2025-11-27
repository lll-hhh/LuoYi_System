package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.common.constant.Constants;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.CargoDeclaration;
import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.exception.ResourceNotFoundException;
import com.lorries.mobile.exception.UnauthorizedException;
import com.lorries.mobile.mapper.CargoDeclarationMapper;
import com.lorries.mobile.service.CargoDeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 货物申报服务实现
 */
@Service
@RequiredArgsConstructor
public class CargoDeclarationServiceImpl extends ServiceImpl<CargoDeclarationMapper, CargoDeclaration>
        implements CargoDeclarationService {

    private static final DateTimeFormatter DECLARATION_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public PageResult<CargoDeclaration> getMyDeclarations(Long userId, Integer page, Integer size, String status) {
        Integer safeUserId = toUserId(userId);
        Page<CargoDeclaration> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CargoDeclaration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CargoDeclaration::getUserId, safeUserId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(CargoDeclaration::getStatus, status);
        }
        wrapper.orderByDesc(CargoDeclaration::getCreatedAt);

        Page<CargoDeclaration> result = page(pageParam, wrapper);
        return PageResult.of(result);
    }

    @Override
    public CargoDeclaration getMyDeclaration(Long userId, Integer declarationId) {
        Integer safeUserId = toUserId(userId);
        CargoDeclaration declaration = getById(declarationId);
        if (declaration == null || !Objects.equals(declaration.getUserId(), safeUserId)) {
            throw new ResourceNotFoundException("货物申报", declarationId.longValue());
        }
        return declaration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CargoDeclaration submit(Long userId, CargoDeclaration declaration) {
        Integer safeUserId = toUserId(userId);
        declaration.setUserId(safeUserId);
        declaration.setDeclarationNo(generateDeclarationNo());
        if (!StringUtils.hasText(declaration.getStatus())) {
            declaration.setStatus(Constants.CARGO_STATUS_PENDING);
        }
        LocalDateTime now = LocalDateTime.now();
        declaration.setCreatedAt(now);
        declaration.setUpdatedAt(now);
        save(declaration);
        return declaration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Integer declarationId) {
        CargoDeclaration declaration = getMyDeclaration(userId, declarationId);
        if ("CANCELLED".equalsIgnoreCase(declaration.getStatus())) {
            throw new BusinessException("申报已取消");
        }
        if ("DELIVERED".equalsIgnoreCase(declaration.getStatus())
                || "COMPLETED".equalsIgnoreCase(declaration.getStatus())) {
            throw new BusinessException("当前状态无法取消");
        }
        declaration.setStatus("CANCELLED");
        declaration.setUpdatedAt(LocalDateTime.now());
        updateById(declaration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer declarationId, String status) {
        CargoDeclaration declaration = getById(declarationId);
        if (declaration == null) {
            throw new ResourceNotFoundException("货物申报", declarationId.longValue());
        }
        declaration.setStatus(status);
        declaration.setUpdatedAt(LocalDateTime.now());
        updateById(declaration);
    }

    private Integer toUserId(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("未获取到登录用户信息");
        }
        return userId.intValue();
    }

    private String generateDeclarationNo() {
        return "CD" + LocalDateTime.now().format(DECLARATION_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
