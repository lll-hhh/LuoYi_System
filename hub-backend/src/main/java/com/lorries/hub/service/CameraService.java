package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Camera;

import java.util.List;

/**
 * 摄像头服务接口
 */
public interface CameraService extends IService<Camera> {

    /**
     * 分页查询摄像头
     */
    PageResult<Camera> findPage(Integer page, Integer size, Integer roadId, String status, String onlineStatus);

    /**
     * 根据ID获取摄像头详情
     */
    Camera findById(Integer cameraId);

    /**
     * 获取在线摄像头列表
     */
    List<Camera> findOnlineCameras();

    /**
     * 更新摄像头心跳
     */
    void updateHeartbeat(Integer cameraId);
}
