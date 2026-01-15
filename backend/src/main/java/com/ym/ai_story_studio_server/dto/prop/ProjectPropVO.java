package com.ym.ai_story_studio_server.dto.prop;

import java.time.LocalDateTime;

/**
 * 项目道具响应VO
 *
 * @param id 项目道具ID
 * @param libraryPropId 全局道具库ID
 * @param displayName 项目内显示名
 * @param name 道具名称(来自库)
 * @param description 描述(项目内覆盖或来自库)
 * @param thumbnailUrl 缩略图URL
 * @param createdAt 创建时间
 */
public record ProjectPropVO(
        Long id,
        Long libraryPropId,
        String displayName,
        String name,
        String description,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
}
