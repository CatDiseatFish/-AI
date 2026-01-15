package com.ym.ai_story_studio_server.dto.project;

import java.time.LocalDateTime;

/**
 * 项目响应对象
 *
 * @param id 项目ID
 * @param folderId 所属文件夹ID
 * @param folderName 所属文件夹名称
 * @param name 项目名称
 * @param aspectRatio 画幅比例
 * @param styleCode 风格标识
 * @param eraSetting 时代设置
 * @param status 项目状态
 * @param shotCount 分镜数量
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ProjectVO(
        Long id,
        Long folderId,
        String folderName,
        String name,
        String aspectRatio,
        String styleCode,
        String eraSetting,
        String status,
        Integer shotCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
