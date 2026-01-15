package com.ym.ai_story_studio_server.dto.invite;

/**
 * 邀请统计响应VO
 *
 * @param totalInvited 总邀请人数
 * @param totalRewardsEarned 总获得积分
 */
public record InviteStatsVO(
        Integer totalInvited,
        Integer totalRewardsEarned
) {}
