package com.ym.ai_story_studio_server.dto.prop;

import java.time.LocalDateTime;

/**
 * 道具分类响应VO
 *
 * @param id 分类ID
 * @param name 分类名称
 * @param sortOrder 排序值(越小越靠前)
 * @param propCount 道具数量统计
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record PropCategoryVO(
        Long id,
        String name,
        Integer sortOrder,
        Integer propCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
