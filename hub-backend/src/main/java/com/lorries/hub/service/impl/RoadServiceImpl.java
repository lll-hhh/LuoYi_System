package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Road;
import com.lorries.hub.mapper.RoadMapper;
import com.lorries.hub.service.RoadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 道路服务实现
 */
@Service
@RequiredArgsConstructor
public class RoadServiceImpl extends ServiceImpl<RoadMapper, Road> implements RoadService {

    @Override
    public PageResult<Road> findPage(Integer page, Integer size, String roadLevel, String status) {
        LambdaQueryWrapper<Road> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(roadLevel)) {
            wrapper.eq(Road::getRoadLevel, roadLevel);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Road::getStatus, status);
        }
        
        wrapper.orderByDesc(Road::getCreatedAt);
        
        Page<Road> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult);
    }

    @Override
    public Road findById(Integer roadId) {
        return getById(roadId);
    }
}
