package com.ym.ai_story_studio_server.dto.ai;

import java.util.List;

/**
 * 图片生成响应DTO
 *
 * <p>AI图片生成服务的响应结果
 *
 * <p><strong>多图支持:</strong> 当模型生成多张图片时(如即梦反代生成4张),
 * imageUrls包含所有生成的图片URL, primaryImageUrl指向第一张(主图片)
 *
 * @param imageUrls 所有生成的图片URL列表(存储在OSS)
 * @param primaryImageUrl 主图片URL(第一张,向后兼容字段)
 * @param jobId 关联的任务ID
 * @param model 使用的模型名称
 * @param aspectRatio 使用的画幅比例
 * @param costPoints 消耗的积分
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ImageGenerateResponse(
        List<String> imageUrls,         // 所有生成的图片URL
        String primaryImageUrl,          // 主图片URL(第一张,向后兼容)
        Long jobId,
        String model,
        String aspectRatio,
        Integer costPoints
) {
    /**
     * 向后兼容构造函数(单图模式)
     *
     * @param imageUrl 单张图片URL
     * @param jobId 任务ID
     * @param model 模型名称
     * @param aspectRatio 画幅比例
     * @param costPoints 积分消耗
     */
    public ImageGenerateResponse(String imageUrl, Long jobId, String model,
                                 String aspectRatio, Integer costPoints) {
        this(List.of(imageUrl), imageUrl, jobId, model, aspectRatio, costPoints);
    }
}
