package com.ym.ai_story_studio_server.dto.character;

import jakarta.validation.constraints.Size;

/**
 * 创建项目内自定义角色请求
 *
 * @param displayName 项目内显示名称
 * @param overrideDescription 项目内覆盖描述
 */
public record CreateProjectCharacterRequest(
        @Size(max = 100, message = "显示名称长度不能超过100个字符")
        String displayName,

        @Size(max = 5000, message = "覆盖描述不能超过5000个字符")
        String overrideDescription
) {
}
