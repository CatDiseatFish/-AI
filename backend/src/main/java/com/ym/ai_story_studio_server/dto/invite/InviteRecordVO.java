package com.ym.ai_story_studio_server.dto.invite;

import java.time.LocalDateTime;

/**
 * 邀请记录响应VO
 *
 * @param id 记录ID
 * @param inviteeUserId 被邀请人用户ID
 * @param inviteeNickname 被邀请人昵称
 * @param inviterReward 邀请人实际获得的积分
 * @param inviteeReward 被邀请人实际获得的积分
 * @param createdAt 邀请成功时间
 */
public record InviteRecordVO(
        Long id,
        Long inviteeUserId,
        String inviteeNickname,
        Integer inviterReward,
        Integer inviteeReward,
        LocalDateTime createdAt
) {}
