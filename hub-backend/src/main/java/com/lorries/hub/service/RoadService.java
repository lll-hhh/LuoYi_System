package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Road;

/**
 * 道路服务接口
 */
public interface RoadService extends IService<Road> {

    /**
     * 分页查询道路
     */
    PageResult<Road> findPage(Integer page, Integer size, String roadLevel, String status);

    /**
     * 根据ID获取道路详情
     */
    Road findById(Integer roadId);
}
