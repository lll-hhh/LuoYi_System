package com.lorries.mobile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("\"user\"")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer userId;

    private String username;
    private String password;
    private String phone;
    private String email;
    private String realName;
    private String idCard;
    private String avatarUrl;
    private String gender;
    private String companyName;
    private String companyLicense;
    private String status;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
