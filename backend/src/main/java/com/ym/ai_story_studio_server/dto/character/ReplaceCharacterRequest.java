package com.ym.ai_story_studio_server.dto.character;

import jakarta.validation.constraints.NotNull;

/**
 * 替换角色请求
 *
 * @param newLibraryCharacterId 新的全局角色ID
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ReplaceCharacterRequest(
        @NotNull(message = "新角色ID不能为空")
        Long newLibraryCharacterId
) {
}
