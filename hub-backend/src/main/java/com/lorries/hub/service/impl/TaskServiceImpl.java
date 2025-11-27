package com.lorries.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.hub.common.result.PageResult;
import com.lorries.hub.entity.Task;
import com.lorries.hub.mapper.TaskMapper;
import com.lorries.hub.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Override
    public PageResult<Task> findPage(Integer page, Integer size, String type, String status, Integer assigneeId) {
        Page<Task> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Task::getTaskTypeId, Integer.parseInt(type));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Task::getStatus, status);
        }
        if (assigneeId != null) {
            wrapper.eq(Task::getAssigneeId, assigneeId);
        }
        
        wrapper.orderByDesc(Task::getCreatedAt);
        Page<Task> result = page(pageParam, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public Task getById(Long id) {
        return baseMapper.selectByIdWithRelations(id);
    }

    @Override
    @Transactional
    public void saveTask(Task task) {
        task.setStatus("pending");
        save(task);
    }

    @Override
    @Transactional
    public void updateTask(Task task) {
        updateById(task);
    }

    @Override
    @Transactional
    public void removeTask(Long id) {
        removeById(id);
    }

    @Override
    @Transactional
    public void assignTask(Long id, Integer assigneeId) {
        Task task = new Task();
        task.setTaskId(id.intValue());
        task.setAssigneeId(assigneeId);
        task.setStatus("assigned");
        updateById(task);
    }

    @Override
    @Transactional
    public void startTask(Long id) {
        Task task = new Task();
        task.setTaskId(id.intValue());
        task.setStatus("in_progress");
        task.setStartTime(LocalDateTime.now());
        updateById(task);
    }

    @Override
    @Transactional
    public void completeTask(Long id, Map<String, Object> result) {
        Task task = new Task();
        task.setTaskId(id.intValue());
        task.setStatus("completed");
        task.setEndTime(LocalDateTime.now());
        if (result != null && result.containsKey("result")) {
            task.setResult((String) result.get("result"));
        }
        updateById(task);
    }

    @Override
    @Transactional
    public void cancelTask(Long id, String reason) {
        Task task = new Task();
        task.setTaskId(id.intValue());
        task.setStatus("cancelled");
        task.setResult("取消原因: " + reason);
        updateById(task);
    }

    @Override
    public List<Task> getMyTasks(Integer userId, String status) {
        return baseMapper.selectByAssignee(userId, status);
    }

    @Override
    public Map<String, Object> getStatistics(Integer assigneeId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (assigneeId != null) {
            stats.put("byStatus", baseMapper.countByStatusForUser(assigneeId));
        } else {
            stats.put("total", count());
            stats.put("byStatus", baseMapper.countByStatus());
            stats.put("byPriority", baseMapper.countByPriority());
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> statisticsByStatus() {
        return baseMapper.countByStatus();
    }

    @Override
    @Transactional
    public void batchAssign(List<Long> taskIds, Integer assigneeId) {
        for (Long taskId : taskIds) {
            assignTask(taskId, assigneeId);
        }
    }
}
