// {{CODE-Cycle-Integration:
//   Task_ID: [#T011]
//   Timestamp: 2025-12-27T10:00:00+08:00
//   Phase: D-Develop
//   Context-Analysis: "Analyzed StylePreset entity, reference FolderVO/UserProfileVO patterns. Creating DTO for style preset response."
//   Principle_Applied: "Verification-Mindset-Loop, KISS, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.dto.stylepreset;

import java.time.LocalDateTime;

/**
 * 风格预设响应VO
 *
 * <p>用于返回风格预设信息,包含风格代码、名称、缩略图、提示词前缀等
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record StylePresetVO(
    /**
     * 风格预设ID
     */
    Long id,

    /**
     * 风格代码(唯一标识)
     * 如: celluloid, pixar, disney3d, ghibli, anime
     */
    String code,

    /**
     * 风格名称
     * 如: 赛璐璐, 皮克斯, 迪士尼3D, 宫崎骏, 日漫
     */
    String name,

    /**
     * 预览缩略图URL
     */
    String thumbnailUrl,

    /**
     * 风格提示词前缀
     * <p>生图时自动拼接到用户提示词前面
     */
    String promptPrefix,

    /**
     * 排序值(越小越靠前)
     */
    Integer sortOrder,

    /**
     * 是否启用: 1启用, 0禁用
     */
    Integer enabled,

    /**
     * 创建时间
     */
    LocalDateTime createdAt
) {}
// {{END_MODIFICATIONS}}
