package com.ym.ai_story_studio_server.dto.character;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 引用角色到项目请求
 *
 * @param libraryCharacterId 全局角色ID
 * @param displayName 项目内显示名称(可选,默认使用全局名称)
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record AddCharacterToProjectRequest(
        @NotNull(message = "全局角色ID不能为空")
        Long libraryCharacterId,

        @Size(min = 1, max = 100, message = "显示名称长度必须在1-100个字符之间")
        String displayName
) {
}
