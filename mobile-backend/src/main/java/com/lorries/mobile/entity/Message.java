package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long messageId;
    
    private Integer userId;
    
    private String title;
    
    private String content;
    
    private String messageType;
    
    private String status;
    
    private String relatedType;
    
    private Long relatedId;
    
    private LocalDateTime readAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
