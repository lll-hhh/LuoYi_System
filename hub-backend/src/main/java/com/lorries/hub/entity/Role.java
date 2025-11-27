package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer roleId;

    private String roleName;
    private String roleCode;
    private String description;
    private Boolean isSystem;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
