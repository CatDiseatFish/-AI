package com.ym.ai_story_studio_server.dto.job;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 任务查询请求
 *
 * <p>用于任务列表的分页查询和条件筛选
 *
 * @param projectId 项目ID筛选(可选)
 * @param status 状态筛选:PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED(可选)
 * @param jobType 任务类型筛选:PARSE_TEXT/GEN_CHAR_IMG/GEN_SCENE_IMG/GEN_SHOT_IMG/GEN_VIDEO/EXPORT_ZIP(可选)
 * @param page 页码(必须大于0)
 * @param size 每页大小(必须在1-100之间)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record JobQueryRequest(
        Long projectId,
        String status,
        String jobType,

        @Min(value = 1, message = "页码必须大于0")
        Integer page,

        @Min(value = 1, message = "每页大小必须大于0")
        @Max(value = 100, message = "每页大小不能超过100")
        Integer size
) {
}
