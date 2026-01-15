package com.ym.ai_story_studio_server.dto.toolbox;

import java.time.LocalDateTime;

/**
 * AI工具箱历史记录VO
 *
 * <p>展示用户在AI工具箱中的生成历史记录
 *
 * <p><strong>数据来源:</strong>
 * <p>从jobs表查询,筛选条件:
 * <ul>
 *   <li>job_type IN ('TOOLBOX_TEXT_GENERATION', 'TOOLBOX_IMAGE_GENERATION', 'TOOLBOX_VIDEO_GENERATION')</li>
 *   <li>user_id = 当前用户ID</li>
 *   <li>created_at >= 7天前(历史记录保留7天)</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * // 文本生成历史
 * ToolboxHistoryVO history = new ToolboxHistoryVO(
 *     123L,
 *     "TEXT",
 *     "gemini-3-pro-preview",
 *     "写一个科幻故事",
 *     null,
 *     "在2157年...",
 *     "SUCCEEDED",
 *     100,
 *     LocalDateTime.now().minusDays(2),
 *     LocalDateTime.now().plusDays(5)
 * );
 *
 * // 图片生成历史
 * ToolboxHistoryVO history = new ToolboxHistoryVO(
 *     124L,
 *     "IMAGE",
 *     "jimeng-4.5",
 *     "赛博朋克风格的城市",
 *     "16:9",
 *     "https://oss.example.com/ai_image_124.jpg",
 *     "SUCCEEDED",
 *     50,
 *     LocalDateTime.now().minusDays(1),
 *     LocalDateTime.now().plusDays(6)
 * );
 * </pre>
 *
 * @param id 任务ID(对应jobs表的id)
 * @param type 生成类型(TEXT/IMAGE/VIDEO)
 * @param model 使用的模型名称
 * @param prompt 原始提示词
 * @param aspectRatio 画幅比例(IMAGE和VIDEO时有值)
 * @param resultContent 生成结果(TEXT为文本内容,IMAGE和VIDEO为OSS URL)
 * @param status 任务状态(PENDING/RUNNING/SUCCEEDED/FAILED)
 * @param costPoints 消耗的积分
 * @param createdAt 创建时间
 * @param expireAt 过期时间(创建时间+7天)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ToolboxHistoryVO(
        /**
         * 任务ID
         *
         * <p>对应jobs表的id字段
         */
        Long id,

        /**
         * 生成类型
         *
         * <p>可选值: TEXT, IMAGE, VIDEO
         */
        String type,

        /**
         * 模型名称
         *
         * <p>从jobs表的meta_json字段中解析
         */
        String model,

        /**
         * 原始提示词
         *
         * <p>从jobs表的meta_json字段中解析
         */
        String prompt,

        /**
         * 画幅比例
         *
         * <p>仅IMAGE和VIDEO类型时有值
         * <p>从jobs表的meta_json字段中解析
         */
        String aspectRatio,

        /**
         * 生成结果
         *
         * <p>TEXT类型: 生成的文本内容
         * <p>IMAGE类型: OSS图片URL
         * <p>VIDEO类型: OSS视频URL
         * <p>从jobs表的meta_json字段中解析
         */
        String resultContent,

        /**
         * 任务状态
         *
         * <p>可选值:
         * <ul>
         *   <li>PENDING - 等待处理</li>
         *   <li>RUNNING - 执行中</li>
         *   <li>SUCCEEDED - 成功完成</li>
         *   <li>FAILED - 执行失败</li>
         *   <li>CANCELED - 已取消</li>
         * </ul>
         */
        String status,

        /**
         * 消耗的积分
         *
         * <p>从usage_charges表查询job_id对应的total_cost
         * <p>如果任务未完成或失败,则为null
         */
        Integer costPoints,

        /**
         * 创建时间
         *
         * <p>任务提交的时间
         */
        LocalDateTime createdAt,

        /**
         * 过期时间
         *
         * <p>创建时间+7天
         * <p>过期后的记录将不再显示在历史列表中
         */
        LocalDateTime expireAt
) {
}
