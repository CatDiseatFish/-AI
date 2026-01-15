package com.ym.ai_story_studio_server.dto.export;

/**
 * 导出响应DTO
 *
 * @param jobId 导出任务ID
 * @param status 任务状态:PENDING/RUNNING/COMPLETED/FAILED
 * @param message 状态消息
 */
public record ExportResponse(
        Long jobId,
        String status,
        String message
) {}
