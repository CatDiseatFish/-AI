package com.ym.ai_story_studio_server.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建场景请求
 *
 * @param categoryId 所属分类ID(可选)
 * @param name 场景名称
 * @param description 场景描述/提示词(可选)
 */
public record CreateSceneRequest(
        Long categoryId,

        @NotBlank(message = "场景名称不能为空")
        @Size(min = 1, max = 100, message = "场景名称长度必须在1-100个字符之间")
        String name,

        @Size(max = 2000, message = "场景描述不能超过2000个字符")
        String description
) {
}
