package com.ym.ai_story_studio_server.dto.character;

import java.time.LocalDateTime;

/**
 * 角色响应VO
 *
 * @param id 角色ID
 * @param categoryId 所属分类ID
 * @param categoryName 所属分类名称
 * @param name 角色名称
 * @param description 角色描述/提示词
 * @param thumbnailUrl 角色缩略图URL
 * @param referenceCount 被项目引用次数统计
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CharacterVO(
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
