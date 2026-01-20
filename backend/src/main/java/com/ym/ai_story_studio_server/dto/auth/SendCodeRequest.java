package com.ym.ai_story_studio_server.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送验证码请求
 *
 * @param phone 手机号
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record SendCodeRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "图形验证码ID不能为空")
        String captchaId,

        @NotBlank(message = "图形验证码不能为空")
        @Pattern(regexp = "^[A-Za-z0-9]{4}$", message = "图形验证码格式不正确")
        String captchaCode
) {
}
