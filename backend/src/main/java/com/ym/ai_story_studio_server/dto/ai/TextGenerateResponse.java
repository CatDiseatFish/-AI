package com.ym.ai_story_studio_server.dto.ai;

/**
 * 文本生成响应DTO
 *
 * <p>AI文本生成服务的响应结果
 *
 * @param text 生成的文本内容
 * @param tokensUsed 实际使用的token数量
 * @param model 使用的模型名称
 * @param costPoints 消耗的积分
 * @param jobId 任务ID（异步生成时返回）
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record TextGenerateResponse(
        String text,
        Integer tokensUsed,
        String model,
        Integer costPoints,
        Long jobId
) {
}
