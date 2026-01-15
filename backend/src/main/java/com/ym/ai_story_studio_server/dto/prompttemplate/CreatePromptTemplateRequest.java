package com.ym.ai_story_studio_server.dto.prompttemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建指令模板请求DTO
 *
 * <p>用于用户创建自定义指令模板
 *
 * @param category 模板类别(CHARACTER/SCENE/SHOT/VIDEO/CUSTOM)
 * @param name 模板名称(1-50字符)
 * @param content 模板内容/提示词文本(1-5000字符)
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreatePromptTemplateRequest(

        @NotBlank(message = "模板类别不能为空")
        @Size(min = 1, max = 20, message = "模板类别长度必须在1-20个字符之间")
        String category,

        @NotBlank(message = "模板名称不能为空")
        @Size(min = 1, max = 50, message = "模板名称长度必须在1-50个字符之间")
        String name,

        @NotBlank(message = "模板内容不能为空")
        @Size(min = 1, max = 5000, message = "模板内容长度必须在1-5000个字符之间")
        String content
) {
}
