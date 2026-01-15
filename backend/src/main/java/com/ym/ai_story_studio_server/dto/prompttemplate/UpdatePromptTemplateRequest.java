package com.ym.ai_story_studio_server.dto.prompttemplate;

import jakarta.validation.constraints.Size;

/**
 * 更新指令模板请求DTO
 *
 * <p>用于更新用户自定义的指令模板,所有字段均为可选
 *
 * @param category 模板类别(可选,CHARACTER/SCENE/SHOT/VIDEO/CUSTOM)
 * @param name 模板名称(可选,1-50字符)
 * @param content 模板内容/提示词文本(可选,1-5000字符)
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record UpdatePromptTemplateRequest(

        @Size(min = 1, max = 20, message = "模板类别长度必须在1-20个字符之间")
        String category,

        @Size(min = 1, max = 50, message = "模板名称长度必须在1-50个字符之间")
        String name,

        @Size(min = 1, max = 5000, message = "模板内容长度必须在1-5000个字符之间")
        String content
) {
}
