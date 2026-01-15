package com.ym.ai_story_studio_server.dto.invite;

import java.time.LocalDateTime;

/**
 * 邀请码响应VO
 *
 * @param id 邀请码ID
 * @param code 邀请码
 * @param rewardPoints 被邀请人获得的积分奖励
 * @param inviterRewardPoints 邀请人获得的积分奖励
 * @param usedCount 已使用次数
 * @param maxUses 最大使用次数(null表示不限制)
 * @param expireAt 过期时间(null表示永不过期)
 * @param enabled 是否启用(1启用,0禁用)
 * @param createdAt 创建时间
 */
public record InviteCodeVO(
        Long id,
        String code,
        Integer rewardPoints,
        Integer inviterRewardPoints,
        Integer usedCount,
        Integer maxUses,
        LocalDateTime expireAt,
        Integer enabled,
        LocalDateTime createdAt
) {}
