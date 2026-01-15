package com.ym.ai_story_studio_server.dto.prop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建道具请求
 *
 * @param categoryId 所属分类ID(可选)
 * @param name 道具名称
 * @param description 道具描述/提示词(可选)
 */
public record CreatePropRequest(
        Long categoryId,

        @NotBlank(message = "道具名称不能为空")
        @Size(min = 1, max = 100, message = "道具名称长度必须在1-100个字符之间")
        String name,

        @Size(max = 2000, message = "道具描述不能超过2000个字符")
        String description,

        @Size(max = 500, message = "缩略图地址长度不能超过500个字符")
        String thumbnailUrl
) {
}
