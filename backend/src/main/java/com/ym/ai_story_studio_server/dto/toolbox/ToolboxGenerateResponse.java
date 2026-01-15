package com.ym.ai_story_studio_server.dto.toolbox;

/**
 * AI工具箱生成响应
 *
 * <p>统一的AI生成响应DTO,包含生成结果和任务信息
 *
 * <p><strong>响应说明:</strong>
 * <ul>
 *   <li>TEXT生成: 同步返回结果,jobId和resultUrl都有值,status为SUCCEEDED</li>
 *   <li>IMAGE生成: 同步返回结果,jobId和resultUrl(OSS URL)都有值,status为SUCCEEDED</li>
 *   <li>VIDEO生成: 异步任务,立即返回jobId和PENDING状态,resultUrl为null</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * // 文本生成响应
 * ToolboxGenerateResponse response = new ToolboxGenerateResponse(
 *     123L,
 *     "SUCCEEDED",
 *     "TEXT",
 *     "gemini-3-pro-preview",
 *     "在2157年...",
 *     null,
 *     null,
 *     100
 * );
 *
 * // 图片生成响应
 * ToolboxGenerateResponse response = new ToolboxGenerateResponse(
 *     124L,
 *     "SUCCEEDED",
 *     "IMAGE",
 *     "jimeng-4.5",
 *     null,
 *     "https://oss.example.com/ai_image_124.jpg",
 *     "16:9",
 *     50
 * );
 *
 * // 视频生成响应(异步)
 * ToolboxGenerateResponse response = new ToolboxGenerateResponse(
 *     125L,
 *     "PENDING",
 *     "VIDEO",
 *     "sora-2",
 *     null,
 *     null,
 *     "16:9",
 *     null  // 积分将在生成成功后扣除
 * );
 * </pre>
 *
 * @param jobId 任务ID,用于查询任务状态
 * @param status 任务状态(PENDING/RUNNING/SUCCEEDED/FAILED)
 * @param type 生成类型(TEXT/IMAGE/VIDEO)
 * @param model 使用的模型名称
 * @param text 生成的文本内容(仅TEXT类型时有值)
 * @param resultUrl 生成的资源URL(IMAGE和VIDEO完成后有值)
 * @param aspectRatio 画幅比例(IMAGE和VIDEO时有值)
 * @param costPoints 消耗的积分(同步生成完成后有值,异步任务完成前为null)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ToolboxGenerateResponse(
        /**
         * 任务ID
         *
         * <p>用于查询任务状态和结果
         * <p>通过 GET /api/jobs/{jobId} 查询任务详情
         */
        Long jobId,

        /**
         * 任务状态
         *
         * <p>可选值:
         * <ul>
         *   <li>PENDING - 任务已提交,等待处理</li>
         *   <li>RUNNING - 任务正在执行中</li>
         *   <li>SUCCEEDED - 任务成功完成</li>
         *   <li>FAILED - 任务执行失败</li>
         * </ul>
         */
        String status,

        /**
         * 生成类型
         *
         * <p>返回请求时指定的生成类型(TEXT/IMAGE/VIDEO)
         */
        String type,

        /**
         * 模型名称
         *
         * <p>实际使用的AI模型名称
         */
        String model,

        /**
         * 生成的文本内容
         *
         * <p>仅当type为TEXT时有值
         * <p>其他类型时为null
         */
        String text,

        /**
         * 生成的资源URL
         *
         * <p>IMAGE: OSS图片URL(同步返回)
         * <p>VIDEO: OSS视频URL(异步任务完成后有值)
         * <p>TEXT: null
         */
        String resultUrl,

        /**
         * 画幅比例
         *
         * <p>仅当type为IMAGE或VIDEO时有值
         * <p>返回实际使用的画幅比例
         */
        String aspectRatio,

        /**
         * 消耗的积分
         *
         * <p>TEXT和IMAGE: 立即扣费,返回扣除的积分数
         * <p>VIDEO: 异步任务,提交时为null,完成后才扣费
         */
        Integer costPoints
) {
}
