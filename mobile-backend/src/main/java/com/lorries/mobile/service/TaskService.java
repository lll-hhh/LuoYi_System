package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.TaskCreateRequest;
import com.lorries.mobile.dto.TaskVO;
import com.lorries.mobile.entity.TransportTask;

/**
 * 任务服务接口
 */
public interface TaskService extends IService<TransportTask> {

    /**
     * 创建任务
     */
    TaskVO createTask(TaskCreateRequest request, Long createdBy);

    /**
     * 获取任务详情
     */
    TaskVO getTaskDetail(Long taskId);

    /**
     * 获取司机任务列表
     */
    PageResult<TaskVO> getDriverTasks(Long driverId, String status, Integer page, Integer pageSize);

    /**
     * 分配任务给司机
     */
    void assignTask(Long taskId, Long driverId, Long vehicleId);

    /**
     * 开始任务
     */
    void startTask(Long taskId, Long driverId);

    /**
     * 完成任务
     */
    void completeTask(Long taskId, Long driverId, Double actualDistance);

    /**
     * 取消任务
     */
    void cancelTask(Long taskId, String reason);

    /**
     * 获取进行中的任务
     */
    TaskVO getCurrentTask(Long driverId);
}
