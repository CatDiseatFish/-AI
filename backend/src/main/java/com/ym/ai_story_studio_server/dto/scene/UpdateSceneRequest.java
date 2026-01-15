package com.ym.ai_story_studio_server.dto.scene;

import jakarta.validation.constraints.Size;

/**
 * 更新场景请求
 *
 * @param categoryId 分类ID(可选)
 * @param name 场景名称(可选)
 * @param description 场景描述/提示词(可选)
 * @param thumbnailUrl 缩略图URL(可选)
 */
public record UpdateSceneRequest(
        Long categoryId,

        @Size(min = 1, max = 100, message = "场景名称长度必须在1-100个字符之间")
        String name,

        @Size(max = 2000, message = "场景描述不能超过2000个字符")
        String description,

        @Size(max = 500, message = "缩略图URL不能超过500个字符")
        String thumbnailUrl
) {
}
