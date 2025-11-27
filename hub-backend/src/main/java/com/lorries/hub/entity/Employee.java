package com.lorries.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
@TableName("employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Integer employeeId;

    private Integer departmentId;
    private Integer roleId;
    private String username;
    private String password;
    private String realName;
    private String gender;
    private String phone;
    private String email;
    private String avatar;
    private String status;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段(非数据库字段)
    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String roleName;

    @TableField(exist = false)
    private String roleCode;
}
