package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 */
public interface MessageService extends IService<Message> {

    /**
     * 分页查询消息
     */
    PageResult<Message> findPage(Integer userId, Integer page, Integer size, String type);

    /**
     * 获取未读消息数量
     */
    Long getUnreadCount(Integer userId);

    /**
     * 标记已读
     */
    void markAsRead(Long id);

    /**
     * 标记所有已读
     */
    void markAllAsRead(Integer userId);

    /**
     * 删除消息
     */
    void removeMessage(Long id);

    /**
     * 批量删除消息
     */
    void batchRemove(List<Long> ids);

    /**
     * 获取消息设置
     */
    Map<String, Object> getSettings(Integer userId);

    /**
     * 更新消息设置
     */
    void updateSettings(Integer userId, Map<String, Object> settings);

    /**
     * 发送消息
     */
    void sendMessage(Message message);
}
