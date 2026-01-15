package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请记录表
 * 记录每一次成功的邀请关系和积分奖励发放情况
 */
@Data
@TableName("invite_records")
public class InviteRecord {

    /**
     * 邀请记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请码ID（关联invite_codes表）
     */
    private Long inviteCodeId;

    /**
     * 邀请人用户ID
     */
    private Long inviterUserId;

    /**
     * 被邀请人用户ID（每个用户只能被邀请一次，有唯一约束）
     */
    private Long inviteeUserId;

    /**
     * 邀请人实际获得的积分（创建记录时固化）
     */
    private Integer inviterReward;

    /**
     * 被邀请人实际获得的积分（创建记录时固化）
     */
    private Integer inviteeReward;

    /**
     * 创建时间（邀请成功时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
