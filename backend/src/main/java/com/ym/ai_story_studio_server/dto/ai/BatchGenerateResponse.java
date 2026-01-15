package com.ym.ai_story_studio_server.dto.ai;

/**
 * 批量生成响应DTO
 *
 * <p>批量生成任务提交成功后的响应,包含任务ID和初始状态
 *
 * <p><strong>任务状态说明:</strong>
 * <ul>
 *   <li>PENDING - 任务已创建,等待执行</li>
 *   <li>RUNNING - 任务正在执行中</li>
 *   <li>SUCCEEDED - 任务已成功完成</li>
 *   <li>FAILED - 任务执行失败</li>
 *   <li>CANCELED - 任务已取消</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * // 提交批量生成任务后
 * BatchGenerateResponse response = batchGenerationService.generateShotsBatch(...);
 *
 * // 立即获得任务ID和状态
 * System.out.println("任务ID: " + response.jobId());
 * System.out.println("状态: " + response.status());
 * System.out.println("目标总数: " + response.totalItems());
 *
 * // 后续通过任务ID查询进度
 * // GET /api/jobs/{jobId}
 * </pre>
 *
 * <p><strong>后续操作:</strong>
 * <ul>
 *   <li>通过jobId查询任务进度: GET /api/jobs/{jobId}</li>
 *   <li>任务进度包含:总数、已完成数、进度百分比</li>
 *   <li>任务完成后可获取所有生成的资产URL列表</li>
 *   <li>可通过任务ID取消正在执行的任务</li>
 * </ul>
 *
 * <p><strong>注意事项:</strong>
 * <ul>
 *   <li>响应立即返回,不等待任务完成</li>
 *   <li>初始状态通常为PENDING或RUNNING</li>
 *   <li>需要轮询或使用WebSocket获取最终结果</li>
 *   <li>任务失败时errorMessage字段会包含错误信息</li>
 * </ul>
 *
 * @param jobId 任务ID,用于后续查询任务状态和结果
 * @param status 任务状态(PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED)
 * @param totalItems 总目标数量(需要生成的目标总数)
 * @param message 提示信息(可选)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record BatchGenerateResponse(
        Long jobId,
        String status,
        Integer totalItems,
        String message
) {
    /**
     * 创建PENDING状态的响应
     *
     * @param jobId 任务ID
     * @param totalItems 总目标数量
     * @return 批量生成响应
     */
    public static BatchGenerateResponse pending(Long jobId, Integer totalItems) {
        return new BatchGenerateResponse(
                jobId,
                "PENDING",
                totalItems,
                "批量生成任务已提交,正在队列中等待执行"
        );
    }

    /**
     * 创建RUNNING状态的响应
     *
     * @param jobId 任务ID
     * @param totalItems 总目标数量
     * @return 批量生成响应
     */
    public static BatchGenerateResponse running(Long jobId, Integer totalItems) {
        return new BatchGenerateResponse(
                jobId,
                "RUNNING",
                totalItems,
                "批量生成任务正在执行中"
        );
    }
}
