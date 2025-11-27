package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体
 */
@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Integer taskId;

    private Integer taskTypeId;
    private String title;
    private String description;
    private Integer assigneeId;
    private String priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String result;
    private Integer createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String taskTypeName;

    @TableField(exist = false)
    private String assigneeName;

    @TableField(exist = false)
    private String creatorName;
}
