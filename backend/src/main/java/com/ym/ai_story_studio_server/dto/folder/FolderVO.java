package com.ym.ai_story_studio_server.dto.folder;

import java.time.LocalDateTime;

/**
 * 文件夹响应对象
 *
 * @param id 文件夹ID
 * @param name 文件夹名称
 * @param sortOrder 排序值（越小越靠前）
 * @param projectCount 项目数量
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record FolderVO(
        Long id,
        String name,
        Integer sortOrder,
        Integer projectCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
