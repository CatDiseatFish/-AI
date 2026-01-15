package com.ym.ai_story_studio_server.dto.invite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 使用邀请码请求
 *
 * @param code 邀请码
 */
public record ApplyInviteRequest(
        @NotBlank(message = "邀请码不能为空")
        @Size(min = 6, max = 10, message = "邀请码长度必须在6-10个字符之间")
        String code
) {}
