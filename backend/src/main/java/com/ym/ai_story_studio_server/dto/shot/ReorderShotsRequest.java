package com.ym.ai_story_studio_server.dto.shot;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 调整分镜顺序请求DTO
 *
 * <p>用于批量调整分镜的排序
 *
 * @param shotIds 按新顺序排列的分镜ID列表,不能为空
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ReorderShotsRequest(
    @NotEmpty(message = "分镜ID列表不能为空")
    List<Long> shotIds
) {}
