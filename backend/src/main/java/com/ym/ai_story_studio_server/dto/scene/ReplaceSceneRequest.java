package com.ym.ai_story_studio_server.dto.scene;

import jakarta.validation.constraints.NotNull;

/**
 * 替换场景请求
 *
 * @param newLibrarySceneId 新的全局场景ID
 */
public record ReplaceSceneRequest(
        @NotNull(message = "新场景ID不能为空")
        Long newLibrarySceneId
) {
}
