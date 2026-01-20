package com.ym.ai_story_studio_server.dto.prop;

import jakarta.validation.constraints.Size;

/**
 * 创建项目内自定义道具请求
 *
 * @param displayName 项目内显示名
 * @param overrideDescription 覆盖描述
 */
public record CreateProjectPropRequest(
        @Size(max = 100, message = "显示名称长度不能超过100个字符")
        String displayName,

        @Size(max = 2000, message = "覆盖描述不能超过2000个字符")
        String overrideDescription
) {
}
