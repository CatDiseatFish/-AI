package com.ym.ai_story_studio_server.dto.scene;

import java.time.LocalDateTime;

/**
 * 场景库响应VO
 *
 * @param id 场景ID
 * @param categoryId 分类ID
 * @param categoryName 分类名称
 * @param name 场景名称
 * @param description 场景描述/提示词
 * @param thumbnailUrl 缩略图URL
 * @param referenceCount 被引用次数
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record SceneVO(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        String thumbnailUrl,
        Integer referenceCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
