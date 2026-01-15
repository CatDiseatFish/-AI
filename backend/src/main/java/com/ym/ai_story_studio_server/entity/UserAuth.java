package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户认证表（支持多登录方式）
 */
@Data
@TableName("user_auth")
public class UserAuth {

    /**
     * 认证记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 认证类型：PHONE/EMAIL/WECHAT
     */
    private String authType;

    /**
     * 唯一标识：手机号/邮箱/微信openid等
     */
    private String identifier;

    /**
     * 凭据：密码哈希（短信/微信可为空）
     */
    private String credential;

    /**
     * 是否已验证：1是，0否
     */
    private Integer verified;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
