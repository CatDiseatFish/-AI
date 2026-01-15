package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请码表
 * 用于用户生成邀请码，邀请新用户注册获得积分奖励
 */
@Data
@TableName("invite_codes")
public class InviteCode {

    /**
     * 邀请码ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请人用户ID
     */
    private Long userId;

    /**
     * 邀请码（唯一标识符）
     */
    private String code;

    /**
     * 被邀请人获得的积分奖励
     */
    private Integer rewardPoints;

    /**
     * 邀请人获得的积分奖励
     */
    private Integer inviterRewardPoints;

    /**
     * 最大使用次数（NULL表示不限制）
     */
    private Integer maxUses;

    /**
     * 已使用次数
     */
    private Integer usedCount;

    /**
     * 过期时间（NULL表示永不过期）
     */
    private LocalDateTime expireAt;

    /**
     * 是否启用：1启用，0禁用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
