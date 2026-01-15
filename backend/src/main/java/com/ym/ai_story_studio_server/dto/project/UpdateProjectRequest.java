package com.ym.ai_story_studio_server.dto.project;

import jakarta.validation.constraints.Size;

/**
 * 更新项目请求
 *
 * @param name 项目名称（可选）
 * @param folderId 所属文件夹ID（可选，为null表示移出文件夹）
 * @param aspectRatio 画幅比例（可选）
 * @param styleCode 风格标识（可选）
 * @param eraSetting 时代设置（可选）
 * @param rawText 小说/剧本文本原文（可选）
 * @param modelConfigJson 模型配置JSON（可选）
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record UpdateProjectRequest(
        /**
         * 项目名称（可选）
         */
        @Size(min = 1, max = 100, message = "项目名称长度必须在1-100个字符之间")
        String name,

        /**
         * 所属文件夹ID（可选，为null表示移出文件夹）
         */
        Long folderId,

        /**
         * 画幅比例（可选）
         */
        String aspectRatio,

        /**
         * 风格标识（可选）
         */
        String styleCode,

        /**
         * 时代设置（可选）
         */
        String eraSetting,

        /**
         * 小说/剧本文本原文（可选）
         */
        @Size(max = 50000, message = "文本内容不能超过50000个字符")
        String rawText,

        /**
         * 模型配置JSON（可选）
         */
        String modelConfigJson
) {
}
