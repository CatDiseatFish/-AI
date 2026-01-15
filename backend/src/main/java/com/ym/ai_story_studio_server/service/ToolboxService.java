package com.ym.ai_story_studio_server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.dto.toolbox.ToolboxGenerateRequest;
import com.ym.ai_story_studio_server.dto.toolbox.ToolboxGenerateResponse;
import com.ym.ai_story_studio_server.dto.toolbox.ToolboxHistoryVO;

/**
 * AI工具箱服务接口
 *
 * <p>提供独立于项目的AI生成工具,支持文本、图片、视频三种生成类型
 *
 * <p><strong>核心功能:</strong>
 * <ul>
 *   <li>统一生成接口: 根据type参数路由到对应的AI服务</li>
 *   <li>历史记录管理: 查询、删除7天内的生成历史</li>
 *   <li>资产保存: 将工具箱生成的结果保存到资产库</li>
 *   <li>自动计费: 调用ChargingService进行积分扣费</li>
 * </ul>
 *
 * <p><strong>数据存储:</strong>
 * <p>所有生成记录存储在jobs表中,通过job_type区分:
 * <ul>
 *   <li>TOOLBOX_TEXT_GENERATION - 工具箱文本生成</li>
 *   <li>TOOLBOX_IMAGE_GENERATION - 工具箱图片生成</li>
 *   <li>TOOLBOX_VIDEO_GENERATION - 工具箱视频生成</li>
 * </ul>
 *
 * <p><strong>7天自动清理:</strong>
 * <p>查询历史记录时自动过滤created_at < 7天前的记录
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface ToolboxService {

    /**
     * 执行AI生成(统一入口)
     *
     * <p>根据request.type()自动路由到对应的AI服务:
     * <ul>
     *   <li>TEXT -> AiTextService</li>
     *   <li>IMAGE -> AiImageService</li>
     *   <li>VIDEO -> AiVideoService(异步)</li>
     * </ul>
     *
     * <p><strong>处理流程:</strong>
     * <ol>
     *   <li>参数验证和类型检查</li>
     *   <li>调用对应的AI生成服务</li>
     *   <li>记录到jobs表(job_type='TOOLBOX_*')</li>
     *   <li>调用ChargingService进行计费</li>
     *   <li>返回生成结果</li>
     * </ol>
     *
     * <p><strong>计费说明:</strong>
     * <ul>
     *   <li>TEXT和IMAGE: 同步生成,立即扣费</li>
     *   <li>VIDEO: 异步生成,任务完成后扣费</li>
     * </ul>
     *
     * @param request 统一生成请求参数
     * @return 生成响应结果
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 当参数无效、余额不足或AI服务调用失败时抛出
     */
    ToolboxGenerateResponse generate(ToolboxGenerateRequest request);

    /**
     * 获取生成历史记录(分页)
     *
     * <p>查询当前用户7天内的工具箱生成历史
     *
     * <p><strong>查询条件:</strong>
     * <ul>
     *   <li>user_id = 当前用户ID</li>
     *   <li>job_type IN ('TOOLBOX_TEXT_GENERATION', 'TOOLBOX_IMAGE_GENERATION', 'TOOLBOX_VIDEO_GENERATION')</li>
     *   <li>created_at >= 7天前</li>
     * </ul>
     *
     * <p><strong>排序:</strong>
     * <p>按created_at降序排列(最新的在前)
     *
     * <p><strong>返回内容:</strong>
     * <ul>
     *   <li>任务基本信息(ID、类型、状态)</li>
     *   <li>生成参数(prompt、model、aspectRatio)</li>
     *   <li>生成结果(text或resultUrl)</li>
     *   <li>积分消耗(从usage_charges表关联查询)</li>
     *   <li>过期时间(created_at + 7天)</li>
     * </ul>
     *
     * @param page 页码(从1开始)
     * @param size 每页大小
     * @return 历史记录分页结果
     */
    Page<ToolboxHistoryVO> getHistory(Integer page, Integer size);

    /**
     * 删除历史记录
     *
     * <p>软删除jobs表中的记录,设置deleted_at字段
     *
     * <p><strong>权限验证:</strong>
     * <p>只能删除自己的记录,删除他人记录会抛出ACCESS_DENIED异常
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>软删除不会删除OSS上的资源文件</li>
     *   <li>已扣除的积分不会退还</li>
     *   <li>删除后无法恢复</li>
     * </ul>
     *
     * @param jobId 任务ID
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 当任务不存在或无权限时抛出
     */
    void deleteHistory(Long jobId);

    /**
     * 保存到资产库
     *
     * <p>将工具箱生成的图片或视频保存到资产库(assets表)
     *
     * <p><strong>支持类型:</strong>
     * <ul>
     *   <li>IMAGE - 创建IMAGE类型的asset记录</li>
     *   <li>VIDEO - 创建VIDEO类型的asset记录</li>
     *   <li>TEXT - 不支持,抛出异常</li>
     * </ul>
     *
     * <p><strong>处理流程:</strong>
     * <ol>
     *   <li>查询job记录,验证状态为SUCCEEDED</li>
     *   <li>验证job类型为IMAGE或VIDEO</li>
     *   <li>从meta_json中提取resultUrl</li>
     *   <li>创建Asset记录</li>
     *   <li>创建AssetVersion记录(version=1)</li>
     *   <li>返回asset_id</li>
     * </ol>
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>只能保存状态为SUCCEEDED的任务</li>
     *   <li>保存后不会删除工具箱历史记录</li>
     *   <li>同一个job可以多次保存,每次创建新的asset</li>
     * </ul>
     *
     * @param jobId 任务ID
     * @return 创建的资产ID
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 当任务不存在、状态错误或类型不支持时抛出
     */
    Long saveToAsset(Long jobId);
}
