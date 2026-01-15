package com.ym.ai_story_studio_server.dto.auth;

/**
 * 手机号登录响应
 *
 * @param token JWT Token
 * @param userId 用户ID
 * @param nickname 昵称
 * @param avatarUrl 头像URL
 * @param balance 积分余额
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record PhoneLoginVO(
        String token,
        Long userId,
        String nickname,
        String avatarUrl,
        Integer balance
) {
}
