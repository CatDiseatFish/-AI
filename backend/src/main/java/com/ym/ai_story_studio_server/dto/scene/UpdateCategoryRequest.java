package com.ym.ai_story_studio_server.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新场景分类请求
 *
 * @param name 分类名称
 */
public record UpdateCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
        String name
) {
}
