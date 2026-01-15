package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.util.UserContext;
import com.ym.ai_story_studio_server.dto.shot.*;
import com.ym.ai_story_studio_server.service.ShotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分镜控制器
 *
 * <p>处理分镜相关接口,需要JWT认证,所有操作都会验证项目归属权限
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/shots")
@RequiredArgsConstructor
public class ShotController {

    private final ShotService shotService;

    /**
     * 获取分镜列表
     *
     * <p>获取指定项目下的所有分镜,按分镜序号升序排序
     *
     * @param projectId 项目ID
     * @return 分镜列表,包含绑定的角色/场景信息和资产状态
     */
    @GetMapping
    public Result<List<ShotVO>> getShotList(@PathVariable("projectId") Long projectId) {
        log.info("收到获取分镜列表请求: projectId={}", projectId);
        Long userId = UserContext.getUserId();
        List<ShotVO> shots = shotService.getShotList(userId, projectId);
        log.info("返回{}条分镜", shots.size());
        return Result.success(shots);
    }

    /**
     * 新增分镜
     *
     * <p>在指定项目下创建新分镜,自动分配分镜序号
     *
     * @param projectId 项目ID
     * @param request 创建分镜请求,包含剧本文本
     * @return 新创建的分镜VO
     */
    @PostMapping
    public Result<ShotVO> createShot(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody CreateShotRequest request) {
        log.info("收到创建分镜请求: projectId={}, scriptText={}", projectId, request.scriptText());
        Long userId = UserContext.getUserId();
        ShotVO shot = shotService.createShot(userId, projectId, request);
        log.info("分镜创建成功: shotId={}, shotNo={}", shot.id(), shot.shotNo());
        return Result.success("分镜创建成功", shot);
    }

    /**
     * AI解析剧本并批量创建分镜
     *
     * <p>将完整剧本文本通过AI拆分成多条分镜,并批量创建
     *
     * @param projectId 项目ID
     * @param request 包含完整剧本的请求
     * @return 创建的分镜列表
     */
    @PostMapping("/parse-script")
    public Result<List<ShotVO>> parseAndCreateShots(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ParseScriptRequest request) {
        log.info("收到AI解析剧本请求: projectId={}, scriptLength={}", projectId, request.fullScript().length());
        Long userId = UserContext.getUserId();
        List<ShotVO> shots = shotService.parseScriptAndCreateShots(userId, projectId, request);
        log.info("AI解析剧本完成: 创建了{}条分镜", shots.size());
        return Result.success("解析成功，已创建 " + shots.size() + " 条分镜", shots);
    }

    /**
     * 更新分镜
     *
     * <p>更新指定分镜的剧本文本
     *
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @param request 更新分镜请求,包含新的剧本文本
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateShot(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long shotId,
            @Valid @RequestBody UpdateShotRequest request) {
        log.info("收到更新分镜请求: projectId={}, shotId={}, newScriptText={}",
                projectId, shotId, request.scriptText());
        Long userId = UserContext.getUserId();
        shotService.updateShot(userId, projectId, shotId, request);
        log.info("分镜更新成功: shotId={}", shotId);
        return Result.success();
    }

    /**
     * 删除分镜
     *
     * <p>软删除指定分镜,同时删除相关的绑定关系
     *
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteShot(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long shotId) {
        log.info("收到删除分镜请求: projectId={}, shotId={}", projectId, shotId);
        Long userId = UserContext.getUserId();
        shotService.deleteShot(userId, projectId, shotId);
        log.info("分镜删除成功: shotId={}", shotId);
        return Result.success();
    }

    /**
     * 调整分镜顺序
     *
     * <p>批量调整分镜的排序,按照传入的shotIds顺序重新分配序号
     *
     * @param projectId 项目ID
     * @param request 调整顺序请求,包含按新顺序排列的分镜ID列表
     * @return 成功响应
     */
    @PutMapping("/reorder")
    public Result<Void> reorderShots(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ReorderShotsRequest request) {
        log.info("收到调整分镜顺序请求: projectId={}, shotIds={}", projectId, request.shotIds());
        Long userId = UserContext.getUserId();
        shotService.reorderShots(userId, projectId, request);
        log.info("分镜顺序调整成功,共调整{}条分镜", request.shotIds().size());
        return Result.success();
    }

    /**
     * 创建绑定关系
     *
     * <p>在分镜与角色/场景之间创建绑定关系
     *
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @param request 创建绑定请求,包含绑定类型和绑定对象ID
     * @return 成功响应
     */
    @PostMapping("/{id}/bindings")
    public Result<Void> createBinding(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long shotId,
            @Valid @RequestBody CreateBindingRequest request) {
        log.info("收到创建绑定关系请求: projectId={}, shotId={}, bindType={}, bindId={}",
                projectId, shotId, request.bindType(), request.bindId());
        Long userId = UserContext.getUserId();
        shotService.createBinding(userId, projectId, shotId, request);
        log.info("绑定关系创建成功: shotId={}, bindType={}, bindId={}",
                shotId, request.bindType(), request.bindId());
        return Result.success();
    }

    /**
     * 删除绑定关系
     *
     * <p>删除指定的分镜绑定关系
     *
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @param bindingId 绑定记录ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}/bindings/{bindingId}")
    public Result<Void> deleteBinding(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long shotId,
            @PathVariable("bindingId") Long bindingId) {
        log.info("收到删除绑定关系请求: projectId={}, shotId={}, bindingId={}",
                projectId, shotId, bindingId);
        Long userId = UserContext.getUserId();
        shotService.deleteBinding(userId, projectId, shotId, bindingId);
        log.info("绑定关系删除成功: bindingId={}", bindingId);
        return Result.success();
    }

    /**
     * 删除分镜图资产
     *
     * <p>删除指定分镜的分镜图资产，删除后状态变为“待生成”
     *
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}/assets/shot-image")
    public Result<Void> deleteShotImageAsset(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long shotId) {
        log.info("收到删除分镜图资产请求: projectId={}, shotId={}", projectId, shotId);
        Long userId = UserContext.getUserId();
        shotService.deleteShotImageAsset(userId, projectId, shotId);
        log.info("分镜图资产删除成功: shotId={}", shotId);
        return Result.success();
    }

    /**
     * 删除视频资产
     *
     * <p>删除指定分镜的视频资产，删除后状态变为“待生成”
     *
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}/assets/video")
    public Result<Void> deleteVideoAsset(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long shotId) {
        log.info("收到删除视频资产请求: projectId={}, shotId={}", projectId, shotId);
        Long userId = UserContext.getUserId();
        shotService.deleteVideoAsset(userId, projectId, shotId);
        log.info("视频资产删除成功: shotId={}", shotId);
        return Result.success();
    }
}
