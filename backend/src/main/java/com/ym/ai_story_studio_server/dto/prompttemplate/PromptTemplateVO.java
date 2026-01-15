package com.ym.ai_story_studio_server.dto.prompttemplate;

import java.time.LocalDateTime;

/**
 * 指令模板响应VO
 *
 * <p>包含模板的完整信息,用于返回给前端
 *
 * @param id 模板ID
 * @param category 模板类别(CHARACTER/SCENE/SHOT/VIDEO/CUSTOM)
 * @param name 模板名称
 * @param content 模板内容/提示词文本
 * @param isSystem 是否系统预设(1=系统预设,0=用户自定义)
 * @param sortOrder 排序值(越小越靠前)
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record PromptTemplateVO(
        Long id,
        String category,
        String name,
        String content,
        Integer isSystem,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
