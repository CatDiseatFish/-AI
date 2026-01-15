package com.ym.ai_story_studio_server.dto.ai;

/**
 * 视频生成响应DTO
 *
 * <p>AI视频生成服务的响应结果
 *
 * <p>注意:视频生成是异步任务,需要通过jobId查询任务进度和最终结果
 *
 * @param jobId 关联的任务ID(用于查询生成进度)
 * @param status 任务状态(PENDING/RUNNING/SUCCEEDED/FAILED)
 * @param model 使用的模型名称
 * @param aspectRatio 使用的画幅比例
 * @param duration 视频时长(秒)
 * @param costPoints 消耗的积分
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record VideoGenerateResponse(
        Long jobId,
        String status,
        String model,
        String aspectRatio,
        Integer duration,
        Integer costPoints
) {
}
