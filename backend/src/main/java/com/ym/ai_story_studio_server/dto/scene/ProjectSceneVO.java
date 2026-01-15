package com.ym.ai_story_studio_server.dto.scene;

import java.time.LocalDateTime;

/**
 * 项目场景响应VO
 *
 * @param id 项目场景ID
 * @param projectId 项目ID
 * @param librarySceneId 全局场景ID
 * @param librarySceneName 全局场景名称
 * @param displayName 项目内显示名称(优先)
 * @param finalDescription 最终描述(项目覆盖 or 全局)
 * @param overrideDescription 项目内覆盖描述
 * @param thumbnailUrl 缩略图URL(从场景库获取)
 * @param createdAt 创建时间
 */
public record ProjectSceneVO(
        Long id,
        Long projectId,
        Long librarySceneId,
        String librarySceneName,
        String displayName,
        String finalDescription,
        String overrideDescription,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
}
