package com.ym.ai_story_studio_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.toolbox.ToolboxGenerateRequest;
import com.ym.ai_story_studio_server.dto.toolbox.ToolboxGenerateResponse;
import com.ym.ai_story_studio_server.dto.toolbox.ToolboxHistoryVO;
import com.ym.ai_story_studio_server.service.ToolboxService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI工具箱控制器
 *
 * <p>提供独立于项目的AI生成工具REST API接口
 *
 * <p><strong>功能特性:</strong>
 * <ul>
 *   <li>支持文本、图片、视频三种生成类型</li>
 *   <li>生成记录保留7天</li>
 *   <li>自动积分计费</li>
 *   <li>可将历史记录保存到资产库</li>
 * </ul>
 *
 * <p><strong>接口列表:</strong>
 * <ul>
 *   <li>POST /api/toolbox/generate - 统一生成接口</li>
 *   <li>GET /api/toolbox/history - 获取历史记录(分页)</li>
 *   <li>DELETE /api/toolbox/history/{id} - 删除历史记录</li>
 *   <li>POST /api/toolbox/history/{id}/save - 保存到资产库</li>
 * </ul>
 *
 * <p><strong>认证要求:</strong>
 * <p>所有接口都需要用户登录认证(JWT Token)
 *
 * <p><strong>响应格式:</strong>
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... },
 *   "timestamp": 1640000000000
 * }
 * </pre>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/toolbox")
@RequiredArgsConstructor
public class ToolboxController {

    private final ToolboxService toolboxService;

    /**
     * AI工具箱生成接口
     *
     * <p><strong>功能描述:</strong><br>
     * 统一的AI生成接口,根据type参数自动路由到对应的AI服务
     *
     * <p><strong>支持的生成类型:</strong>
     * <ul>
     *   <li>TEXT - 文本生成(同步,立即返回结果)</li>
     *   <li>IMAGE - 图片生成(同步,立即返回OSS URL)</li>
     *   <li>VIDEO - 视频生成(异步,返回任务ID)</li>
     * </ul>
     *
     * <p><strong>请求示例(文本生成):</strong>
     * <pre>
     * POST /api/toolbox/generate
     * Content-Type: application/json
     *
     * {
     *   "type": "TEXT",
     *   "prompt": "写一个关于AI的科幻短故事",
     *   "model": null,
     *   "aspectRatio": null,
     *   "duration": null,
     *   "referenceImageUrl": null
     * }
     * </pre>
     *
     * <p><strong>响应示例(文本生成):</strong>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "文本生成成功",
     *   "data": {
     *     "jobId": null,
     *     "status": "SUCCEEDED",
     *     "type": "TEXT",
     *     "model": "gemini-3-pro-preview",
     *     "text": "在2157年的新上海...",
     *     "resultUrl": null,
     *     "aspectRatio": null,
     *     "costPoints": 100
     *   },
     *   "timestamp": 1640000000000
     * }
     * </pre>
     *
     * <p><strong>请求示例(图片生成):</strong>
     * <pre>
     * POST /api/toolbox/generate
     * Content-Type: application/json
     *
     * {
     *   "type": "IMAGE",
     *   "prompt": "赛博朋克风格的未来城市,霓虹灯,夜景",
     *   "model": "gemini-3-pro-image-preview",
     *   "aspectRatio": "16:9",
     *   "duration": null,
     *   "referenceImageUrl": null
     * }
     * </pre>
     *
     * <p><strong>响应示例(图片生成):</strong>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "图片生成成功",
     *   "data": {
     *     "jobId": 123,
     *     "status": "SUCCEEDED",
     *     "type": "IMAGE",
     *     "model": "gemini-3-pro-image-preview",
     *     "text": null,
     *     "resultUrl": "https://oss.example.com/ai_image_123.jpg",
     *     "aspectRatio": "16:9",
     *     "costPoints": 50
     *   },
     *   "timestamp": 1640000000000
     * }
     * </pre>
     *
     * <p><strong>请求示例(视频生成):</strong>
     * <pre>
     * POST /api/toolbox/generate
     * Content-Type: application/json
     *
     * {
     *   "type": "VIDEO",
     *   "prompt": "机器人在未来城市中行走,镜头缓缓推进",
     *   "model": null,
     *   "aspectRatio": "16:9",
     *   "duration": 5,
     *   "referenceImageUrl": null
     * }
     * </pre>
     *
     * <p><strong>响应示例(视频生成-异步):</strong>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "视频生成任务已提交,请通过jobId查询任务进度",
     *   "data": {
     *     "jobId": 456,
     *     "status": "PENDING",
     *     "type": "VIDEO",
     *     "model": "sora-2",
     *     "text": null,
     *     "resultUrl": null,
     *     "aspectRatio": "16:9",
     *     "costPoints": null
     *   },
     *   "timestamp": 1640000000000
     * }
     * </pre>
     *
     * <p><strong>参数说明:</strong>
     * <ul>
     *   <li>type - 生成类型,必填,可选值:TEXT/IMAGE/VIDEO</li>
     *   <li>prompt - 提示词,必填,1-10000字符</li>
     *   <li>model - 模型名称,可选,不指定则使用默认模型</li>
     *   <li>aspectRatio - 画幅比例,可选,IMAGE和VIDEO时可用</li>
     *   <li>duration - 视频时长,可选,VIDEO时可用,1-10秒</li>
     *   <li>referenceImageUrl - 参考图URL,可选,用于图生图或图生视频</li>
     * </ul>
     *
     * <p><strong>计费说明:</strong>
     * <ul>
     *   <li>TEXT和IMAGE: 同步生成,立即扣费并返回costPoints</li>
     *   <li>VIDEO: 异步生成,任务提交时不扣费,完成后自动扣费</li>
     *   <li>扣费失败会抛出异常,不会生成结果</li>
     * </ul>
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>工具箱生成的记录会保留7天,超过7天自动过期</li>
     *   <li>过期的记录不会显示在历史列表中,但不会自动删除</li>
     *   <li>可通过保存到资产库功能永久保存生成结果</li>
     * </ul>
     *
     * @param request 生成请求参数
     * @return 生成响应结果
     */
    @PostMapping("/generate")
    public Result<ToolboxGenerateResponse> generate(@Valid @RequestBody ToolboxGenerateRequest request) {
        log.info("收到AI工具箱生成请求 - type: {}, promptLength: {}",
                request.type(), request.prompt().length());

        ToolboxGenerateResponse response = toolboxService.generate(request);

        String message = switch (request.type()) {
            case "TEXT" -> "文本生成成功";
            case "IMAGE" -> "图片生成成功";
            case "VIDEO" -> "视频生成任务已提交,请通过jobId查询任务进度";
            default -> "生成成功";
        };

        log.info("AI工具箱生成完成 - type: {}, status: {}", request.type(), response.status());

        return Result.success(message, response);
    }

    /**
     * 获取生成历史记录(分页)
     *
     * <p><strong>功能描述:</strong><br>
     * 查询当前用户7天内的工具箱生成历史,按创建时间倒序排列
     *
     * <p><strong>请求示例:</strong>
     * <pre>
     * GET /api/toolbox/history?page=1&size=10
     * </pre>
     *
     * <p><strong>响应示例:</strong>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "records": [
     *       {
     *         "id": 123,
     *         "type": "TEXT",
     *         "model": "gemini-3-pro-preview",
     *         "prompt": "写一个科幻故事",
     *         "aspectRatio": null,
     *         "resultContent": "在2157年...",
     *         "status": "SUCCEEDED",
     *         "costPoints": 100,
     *         "createdAt": "2025-12-28T10:00:00",
     *         "expireAt": "2026-01-04T10:00:00"
     *       },
     *       {
     *         "id": 124,
     *         "type": "IMAGE",
     *         "model": "jimeng-4.5",
     *         "prompt": "赛博朋克城市",
     *         "aspectRatio": "16:9",
     *         "resultContent": "https://oss.example.com/ai_image_124.jpg",
     *         "status": "SUCCEEDED",
     *         "costPoints": 50,
     *         "createdAt": "2025-12-28T09:30:00",
     *         "expireAt": "2026-01-04T09:30:00"
     *       }
     *     ],
     *     "total": 15,
     *     "size": 10,
     *     "current": 1,
     *     "pages": 2
     *   },
     *   "timestamp": 1640000000000
     * }
     * </pre>
     *
     * <p><strong>参数说明:</strong>
     * <ul>
     *   <li>page - 页码,从1开始,默认1</li>
     *   <li>size - 每页大小,默认10,最大100</li>
     * </ul>
     *
     * <p><strong>查询规则:</strong>
     * <ul>
     *   <li>只查询当前用户的记录</li>
     *   <li>只查询7天内的记录(created_at >= 7天前)</li>
     *   <li>只查询未删除的记录(deleted_at IS NULL)</li>
     *   <li>按创建时间倒序排列(最新的在前)</li>
     * </ul>
     *
     * <p><strong>返回字段说明:</strong>
     * <ul>
     *   <li>resultContent - TEXT类型为文本内容,IMAGE和VIDEO类型为OSS URL</li>
     *   <li>costPoints - 消耗的积分,异步任务未完成时为null</li>
     *   <li>expireAt - 过期时间,创建时间+7天</li>
     * </ul>
     *
     * @param page 页码,从1开始,默认1
     * @param size 每页大小,默认10,最大100
     * @return 历史记录分页结果
     */
    @GetMapping("/history")
    public Result<Page<ToolboxHistoryVO>> getHistory(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size
    ) {
        log.info("查询AI工具箱历史 - page: {}, size: {}", page, size);

        Page<ToolboxHistoryVO> result = toolboxService.getHistory(page, size);

        log.info("查询AI工具箱历史完成 - total: {}, pages: {}", result.getTotal(), result.getPages());

        return Result.success(result);
    }

    /**
     * 删除历史记录
     *
     * <p><strong>功能描述:</strong><br>
     * 软删除指定的工具箱生成记录
     *
     * <p><strong>请求示例:</strong>
     * <pre>
     * DELETE /api/toolbox/history/123
     * </pre>
     *
     * <p><strong>响应示例:</strong>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "历史记录删除成功",
     *   "timestamp": 1640000000000
     * }
     * </pre>
     *
     * <p><strong>权限验证:</strong>
     * <p>只能删除自己的记录,删除他人记录会返回403错误
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>软删除,设置deleted_at字段,不物理删除数据</li>
     *   <li>不会删除OSS上的资源文件</li>
     *   <li>已扣除的积分不会退还</li>
     *   <li>删除后无法恢复</li>
     * </ul>
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable("id") Long id) {
        log.info("删除AI工具箱历史 - id: {}", id);

        toolboxService.deleteHistory(id);

        log.info("删除AI工具箱历史成功 - id: {}", id);

        return Result.success();
    }

    /**
     * 保存到资产库
     *
     * <p><strong>功能描述:</strong><br>
     * 将工具箱生成的图片或视频永久保存到资产库
     *
     * <p><strong>请求示例:</strong>
     * <pre>
     * POST /api/toolbox/history/123/save
     * </pre>
     *
     * <p><strong>响应示例:</strong>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "保存成功",
     *   "data": {
     *     "assetId": 789
     *   },
     *   "timestamp": 1640000000000
     * }
     * </pre>
     *
     * <p><strong>支持的类型:</strong>
     * <ul>
     *   <li>IMAGE - 创建IMAGE类型的资产</li>
     *   <li>VIDEO - 创建VIDEO类型的资产</li>
     *   <li>TEXT - 不支持,返回400错误</li>
     * </ul>
     *
     * <p><strong>保存逻辑:</strong>
     * <ol>
     *   <li>创建Asset记录(asset_type=IMAGE或VIDEO)</li>
     *   <li>创建AssetVersion记录(version=1,storage_url=resultUrl)</li>
     *   <li>更新Asset的current_version_id</li>
     *   <li>返回asset_id</li>
     * </ol>
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>只能保存状态为SUCCEEDED的任务</li>
     *   <li>保存后不会删除工具箱历史记录</li>
     *   <li>同一个job可以多次保存,每次创建新的asset</li>
     *   <li>保存的资产可在资产库中查看和管理</li>
     * </ul>
     *
     * @param id 任务ID
     * @return 创建的资产ID
     */
    @PostMapping("/history/{id}/save")
    public Result<Long> saveToAsset(@PathVariable("id") Long id) {
        log.info("保存AI工具箱结果到资产库 - id: {}", id);

        Long assetId = toolboxService.saveToAsset(id);

        log.info("保存AI工具箱结果成功 - assetId: {}", assetId);

        return Result.success("保存成功", assetId);
    }
}
