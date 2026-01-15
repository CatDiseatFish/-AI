package com.ym.ai_story_studio_server.dto.user;

import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求
 *
 * @param nickname 昵称（2-20个字符）
 * @param avatarUrl 头像URL（最长512字符）
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record UpdateProfileRequest(
        @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
        String nickname,

        @Size(max = 512, message = "头像URL长度不能超过512字符")
        String avatarUrl
) {
}
