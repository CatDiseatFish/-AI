package com.ym.ai_story_studio_server.dto.auth;

/**
 * 图形验证码响应
 *
 * @param captchaId 验证码ID
 * @param imageBase64 验证码图片Base64（data URI）
 */
public record CaptchaResponse(
        String captchaId,
        String imageBase64
) {
}
