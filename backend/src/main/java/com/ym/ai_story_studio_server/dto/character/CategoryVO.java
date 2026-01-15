package com.ym.ai_story_studio_server.dto.character;

import java.time.LocalDateTime;

/**
 * 角色分类响应VO
 *
 * @param id 分类ID
 * @param name 分类名称
 * @param sortOrder 排序值(越小越靠前)
 * @param characterCount 分类下角色数量统计
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CategoryVO(
        Long id,
        String name,
        Integer sortOrder,
        Integer characterCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
