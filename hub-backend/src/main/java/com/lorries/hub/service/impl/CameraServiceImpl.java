package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Camera;
import com.lorries.hub.mapper.CameraMapper;
import com.lorries.hub.service.CameraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 摄像头服务实现
 */
@Service
@RequiredArgsConstructor
public class CameraServiceImpl extends ServiceImpl<CameraMapper, Camera> implements CameraService {

    @Override
    public PageResult<Camera> findPage(Integer pageNum, Integer pageSize, Integer roadId, String status, String onlineStatus) {
        LambdaQueryWrapper<Camera> wrapper = new LambdaQueryWrapper<>();
        if (roadId != null) {
            wrapper.eq(Camera::getRoadId, roadId);
        }
        if (status != null) {
            wrapper.eq(Camera::getStatus, status);
        }
        if (onlineStatus != null) {
            wrapper.eq(Camera::getOnlineStatus, onlineStatus);
        }
        wrapper.orderByDesc(Camera::getCameraId);
        IPage<Camera> pageResult = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(pageResult);
    }

    @Override
    public Camera findById(Integer cameraId) {
        return getById(cameraId);
    }

    @Override
    public List<Camera> findOnlineCameras() {
        LambdaQueryWrapper<Camera> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Camera::getOnlineStatus, "ONLINE")
               .eq(Camera::getStatus, "ACTIVE");
        return list(wrapper);
    }

    @Override
    public void updateHeartbeat(Integer cameraId) {
        Camera camera = new Camera();
        camera.setCameraId(cameraId);
        camera.setOnlineStatus("ONLINE");
        camera.setLastHeartbeat(LocalDateTime.now());
        updateById(camera);
    }
}
