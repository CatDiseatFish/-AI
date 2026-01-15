package com.ym.ai_story_studio_server.dto.prop;

import jakarta.validation.constraints.NotNull;

/**
 * 添加道具到项目请求
 *
 * @param libraryPropId 全局道具库ID
 * @param displayName 项目内显示名(可选)
 * @param overrideDescription 覆盖描述(可选)
 */
public record AddPropToProjectRequest(
        @NotNull(message = "道具库ID不能为空")
        Long libraryPropId,

        String displayName,

        String overrideDescription
) {
}
