package com.ym.ai_story_studio_server.dto.character;

import java.time.LocalDateTime;

/**
 * 项目角色响应VO
 *
 * @param id 项目角色ID
 * @param projectId 项目ID
 * @param libraryCharacterId 全局角色ID
 * @param libraryCharacterName 全局角色名称
 * @param displayName 项目内显示名称
 * @param finalDescription 最终生效的描述(优先使用覆盖描述,否则使用全局描述)
 * @param overrideDescription 项目内覆盖描述
 * @param thumbnailUrl 角色缩略图URL
 * @param createdAt 创建时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ProjectCharacterVO(
        Long id,
        Long projectId,
        Long libraryCharacterId,
        String libraryCharacterName,
        String displayName,
        String finalDescription,
        String overrideDescription,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
}
