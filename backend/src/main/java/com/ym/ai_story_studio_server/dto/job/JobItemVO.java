package com.ym.ai_story_studio_server.dto.job;

import java.time.LocalDateTime;

/**
 * 子任务响应VO
 *
 * <p>用于任务详情中展示子任务信息
 *
 * @param id 子任务ID
 * @param targetType 目标对象类型:LIB_CHAR/PCHAR/LIB_SCENE/PSCENE/SHOT/PROJECT
 * @param targetId 目标对象ID
 * @param status 子任务状态:PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED
 * @param outputAssetVersionId 输出资产版本ID(成功后写入)
 * @param errorMessage 子任务错误信息
 * @param startedAt 子任务开始时间
 * @param finishedAt 子任务结束时间
 * @param createdAt 创建时间
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record JobItemVO(
        Long id,
        String targetType,
        Long targetId,
        String status,
        Long outputAssetVersionId,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
