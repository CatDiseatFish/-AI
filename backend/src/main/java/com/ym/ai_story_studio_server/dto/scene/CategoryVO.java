package com.ym.ai_story_studio_server.dto.scene;

import java.time.LocalDateTime;

/**
 * 场景分类响应VO
 *
 * @param id 分类ID
 * @param name 分类名称
 * @param sortOrder 排序值(越小越靠前)
 * @param sceneCount 场景数量统计
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record CategoryVO(
        Long id,
        String name,
        Integer sortOrder,
        Integer sceneCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
