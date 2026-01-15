package com.ym.ai_story_studio_server.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建项目请求
 *
 * @param name 项目名称
 * @param folderId 所属文件夹ID（可选）
 * @param aspectRatio 画幅比例：16:9/9:16/21:9等
 * @param styleCode 风格标识
 * @param eraSetting 时代设置（可选）
 * @param rawText 小说/剧本文本原文（可选）
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateProjectRequest(
        /**
         * 项目名称
         */
        @NotBlank(message = "项目名称不能为空")
        @Size(min = 1, max = 100, message = "项目名称长度必须在1-100个字符之间")
        String name,

        /**
         * 所属文件夹ID（可选，为null表示未归类）
         */
        Long folderId,

        /**
         * 画幅比例：16:9/9:16/21:9等
         */
        @NotBlank(message = "画幅比例不能为空")
        String aspectRatio,

        /**
         * 风格标识：如cinematic/anime等
         */
        String styleCode,

        /**
         * 时代设置（如：现代/古代/未来）
         */
        String eraSetting,

        /**
         * 小说/剧本文本原文（可选）
         */
        @Size(max = 50000, message = "文本内容不能超过50000个字符")
        String rawText
) {
}
