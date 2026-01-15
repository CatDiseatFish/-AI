package com.ym.ai_story_studio_server.dto.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建角色请求
 *
 * @param name 角色名称
 * @param categoryId 所属分类ID(可选)
 * @param description 角色描述/提示词
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateCharacterRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(min = 1, max = 100, message = "角色名称长度必须在1-100个字符之间")
        String name,

        Long categoryId,

        @Size(max = 5000, message = "角色描述不能超过5000个字符")
        String description
) {
}
