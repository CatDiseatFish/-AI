package com.ym.ai_story_studio_server.dto.asset;

import jakarta.validation.constraints.NotNull;

/**
 * 设置当前版本请求
 *
 * <p>用于设置资产的当前使用版本,支持版本回退(切换到历史版本)
 *
 * @param versionId 要设置为当前版本的版本ID(必须是有效的版本ID且属于指定资产)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record SetCurrentVersionRequest(
        @NotNull(message = "版本ID不能为空")
        Long versionId
) {}
