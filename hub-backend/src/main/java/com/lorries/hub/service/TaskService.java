package com.lorries.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService extends IService<Task> {

    /**
     * 分页查询任务
     */
    PageResult<Task> findPage(Integer page, Integer size, String type, String status, Integer assigneeId);

    /**
     * 根据ID获取任务
     */
    Task getById(Long id);

    /**
     * 新增任务
     */
    void saveTask(Task task);

    /**
     * 更新任务
     */
    void updateTask(Task task);

    /**
     * 删除任务
     */
    void removeTask(Long id);

    /**
     * 分配任务
     */
    void assignTask(Long id, Integer assigneeId);

    /**
     * 开始任务
     */
    void startTask(Long id);

    /**
     * 完成任务
     */
    void completeTask(Long id, Map<String, Object> result);

    /**
     * 取消任务
     */
    void cancelTask(Long id, String reason);

    /**
     * 获取我的任务
     */
    List<Task> getMyTasks(Integer userId, String status);

    /**
     * 获取任务统计
     */
    Map<String, Object> getStatistics(Integer assigneeId);

    /**
     * 按状态统计
     */
    List<Map<String, Object>> statisticsByStatus();

    /**
     * 批量分配任务
     */
    void batchAssign(List<Long> taskIds, Integer assigneeId);
}
