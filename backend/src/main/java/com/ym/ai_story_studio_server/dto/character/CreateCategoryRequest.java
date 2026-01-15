package com.ym.ai_story_studio_server.dto.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建角色分类请求
 *
 * @param name 分类名称
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
        String name
) {
}
