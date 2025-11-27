package com.lorries.mobile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.Message;
import com.lorries.mobile.mapper.MessageMapper;
import com.lorries.mobile.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public PageResult<Message> findPage(Integer userId, Integer page, Integer size, String type) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Message::getUserId, userId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Message::getMessageType, type);
        }
        wrapper.orderByDesc(Message::getCreatedAt);
        
        Page<Message> result = page(pageParam, wrapper);
        return PageResult.of(result);
    }

    @Override
    public Long getUnreadCount(Integer userId) {
        return baseMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Message message = new Message();
        message.setMessageId(id);
        message.setStatus("read");
        message.setReadAt(LocalDateTime.now());
        updateById(message);
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        baseMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void removeMessage(Long id) {
        removeById(id);
    }

    @Override
    @Transactional
    public void batchRemove(List<Long> ids) {
        removeByIds(ids);
    }

    @Override
    public Map<String, Object> getSettings(Integer userId) {
        // 简化实现：返回默认设置
        Map<String, Object> settings = new HashMap<>();
        settings.put("pushEnabled", true);
        settings.put("soundEnabled", true);
        settings.put("vibrationEnabled", true);
        settings.put("nightModeEnabled", false);
        return settings;
    }

    @Override
    public void updateSettings(Integer userId, Map<String, Object> settings) {
        // 简化实现：保存设置
    }

    @Override
    @Transactional
    public void sendMessage(Message message) {
        message.setStatus("unread");
        save(message);
    }
}
