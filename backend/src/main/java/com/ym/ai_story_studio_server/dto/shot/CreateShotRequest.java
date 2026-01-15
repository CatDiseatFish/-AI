package com.ym.ai_story_studio_server.dto.shot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分镜请求DTO
 *
 * <p>用于创建新的分镜记录
 *
 * @param scriptText 分镜剧本文本,不能为空,最多2000个字符
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateShotRequest(
    @NotBlank(message = "剧本文本不能为空")
    @Size(max = 2000, message = "剧本文本不能超过2000个字符")
    String scriptText
) {}
