package com.ym.ai_story_studio_server.dto.scene;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 引用场景到项目请求
 *
 * @param librarySceneId 全局场景ID
 * @param displayName 项目内显示名称(可选)
 */
public record AddSceneToProjectRequest(
        @NotNull(message = "场景ID不能为空")
        Long librarySceneId,

        @Size(max = 100, message = "显示名称长度不能超过100个字符")
        String displayName
) {
}
