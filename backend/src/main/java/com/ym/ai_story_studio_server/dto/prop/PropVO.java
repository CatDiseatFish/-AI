package com.ym.ai_story_studio_server.dto.prop;

import java.time.LocalDateTime;

/**
 * 道具库响应VO
 *
 * @param id 道具ID
 * @param categoryId 分类ID
 * @param categoryName 分类名称
 * @param name 道具名称
 * @param description 道具描述/提示词
 * @param thumbnailUrl 缩略图URL
 * @param referenceCount 被引用次数
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record PropVO(
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
