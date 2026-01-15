package com.ym.ai_story_studio_server.dto.user;

import java.time.LocalDateTime;

/**
 * 用户信息响应
 *
 * @param userId 用户ID
 * @param nickname 昵称
 * @param avatarUrl 头像URL
 * @param status 状态：1正常，0禁用
 * @param balance 积分余额
 * @param createdAt 注册时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record UserProfileVO(
        Long userId,
        String nickname,
        String avatarUrl,
        Integer status,
        Integer balance,
        LocalDateTime createdAt
) {
}
