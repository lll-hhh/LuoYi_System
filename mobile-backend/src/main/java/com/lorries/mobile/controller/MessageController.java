package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.entity.Message;
import com.lorries.mobile.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "获取消息列表")
    @GetMapping
    public Result<PageResult<Message>> getMessages(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type) {
        return Result.success(messageService.findPage(userId, page, size, type));
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(@RequestParam Integer userId) {
        return Result.success(messageService.getUnreadCount(userId));
    }

    @Operation(summary = "获取消息详情")
    @GetMapping("/{id}")
    public Result<Message> getMessageById(@PathVariable Long id) {
        return Result.success(messageService.getById(id));
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "标记所有消息已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestParam Integer userId) {
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.removeMessage(id);
        return Result.success();
    }

    @Operation(summary = "批量删除消息")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteMessages(@RequestBody List<Long> ids) {
        messageService.batchRemove(ids);
        return Result.success();
    }

    @Operation(summary = "获取消息设置")
    @GetMapping("/settings")
    public Result<Map<String, Object>> getMessageSettings(@RequestParam Integer userId) {
        return Result.success(messageService.getSettings(userId));
    }

    @Operation(summary = "更新消息设置")
    @PutMapping("/settings")
    public Result<Void> updateMessageSettings(
            @RequestParam Integer userId,
            @RequestBody Map<String, Object> settings) {
        messageService.updateSettings(userId, settings);
        return Result.success();
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public Result<Void> sendMessage(@RequestBody Message message) {
        messageService.sendMessage(message);
        return Result.success();
    }
}
