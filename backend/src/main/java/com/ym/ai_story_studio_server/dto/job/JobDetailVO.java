package com.ym.ai_story_studio_server.dto.job;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务详情响应VO
 *
 * <p>用于任务详情页面展示,包含任务的完整信息和子任务列表
 *
 * @param id 任务ID
 * @param projectId 所属项目ID
 * @param projectName 项目名称
 * @param jobType 任务类型:PARSE_TEXT/GEN_CHAR_IMG/GEN_SCENE_IMG/GEN_SHOT_IMG/GEN_VIDEO/EXPORT_ZIP
 * @param status 任务状态:PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED
 * @param progress 任务进度:0-100
 * @param totalItems 子任务总数(批量生成用)
 * @param doneItems 已完成子任务数
 * @param elapsedSeconds 已耗时(秒)
 * @param startedAt 开始时间(用于计时展示)
 * @param finishedAt 结束时间(可空)
 * @param errorMessage 错误信息(失败时)
 * @param resultUrl 结果URL(生成的图片/视频URL等)
 * @param allImageUrls 所有生成的图片URL列表(仅图片生成任务)
 * @param costPoints 消耗积分
 * @param metaJson 扩展参数(JSON):如模型、比例、导出模式(current/all)等
 * @param items 子任务列表
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record JobDetailVO(
        Long id,
        Long projectId,
        String projectName,
        String jobType,
        String status,
        Integer progress,
        Integer totalItems,
        Integer doneItems,
        Long elapsedSeconds,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String errorMessage,
        String resultUrl,
        List<String> allImageUrls,
        Integer costPoints,
        String metaJson,
        List<JobItemVO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
