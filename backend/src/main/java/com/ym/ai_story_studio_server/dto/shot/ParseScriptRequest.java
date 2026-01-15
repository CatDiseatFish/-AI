package com.ym.ai_story_studio_server.dto.shot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI解析剧本请求DTO
 *
 * <p>用于将完整剧本文本拆分成多条分镜
 *
 * @param fullScript 完整剧本文本,不能为空,最多50000个字符
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ParseScriptRequest(
    @NotBlank(message = "剧本文本不能为空")
    @Size(max = 50000, message = "剧本文本不能超过50000个字符")
    String fullScript
) {}
