package com.ym.ai_story_studio_server.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 手机号登录请求
 *
 * @param phone 手机号
 * @param code 验证码
 * @param inviteCode 邀请码（可选）
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record PhoneLoginRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
        String code,

        String inviteCode
) {
}
