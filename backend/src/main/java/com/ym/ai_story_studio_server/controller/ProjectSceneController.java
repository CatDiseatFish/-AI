package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.scene.AddSceneToProjectRequest;
import com.ym.ai_story_studio_server.dto.scene.CreateProjectSceneRequest;
import com.ym.ai_story_studio_server.dto.scene.ProjectSceneVO;
import com.ym.ai_story_studio_server.dto.scene.ReplaceSceneRequest;
import com.ym.ai_story_studio_server.dto.scene.UpdateProjectSceneRequest;
import com.ym.ai_story_studio_server.service.ProjectSceneService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目场景控制器
 *
 * <p>处理项目场景相关接口,需要JWT认证和项目权限验证
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/scenes")
@RequiredArgsConstructor
public class ProjectSceneController {

    private final ProjectSceneService projectSceneService;

    /**
     * 获取项目场景列表
     *
     * @param projectId 项目ID
     * @return 项目场景列表响应
     */
    @GetMapping
    public Result<List<ProjectSceneVO>> getProjectSceneList(@PathVariable("projectId") Long projectId) {
        Long userId = UserContext.getUserId();
        log.info("获取项目场景列表, userId: {}, projectId: {}", userId, projectId);
        List<ProjectSceneVO> scenes = projectSceneService.getProjectSceneList(userId, projectId);
        return Result.success(scenes);
    }

    /**
     * 引用场景到项目
     *
     * @param projectId 项目ID
     * @param request 引用场景请求
     * @return 项目场景VO响应
     */
    @PostMapping
    public Result<ProjectSceneVO> addSceneToProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody AddSceneToProjectRequest request) {
        Long userId = UserContext.getUserId();
        log.info("引用场景到项目, userId: {}, projectId: {}, librarySceneId: {}",
                userId, projectId, request.librarySceneId());
        ProjectSceneVO scene = projectSceneService.addSceneToProject(userId, projectId, request);
        return Result.success(scene);
    }

    /**
     * 创建项目内自定义场景（不加入场景库）
     */
    @PostMapping("/custom")
    public Result<ProjectSceneVO> createCustomScene(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody CreateProjectSceneRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建项目内自定义场景, userId: {}, projectId: {}", userId, projectId);
        ProjectSceneVO scene = projectSceneService.createCustomScene(userId, projectId, request);
        return Result.success(scene);
    }

    /**
     * 更新项目内场景覆盖
     *
     * @param projectId 项目ID
     * @param projectSceneId 项目场景ID
     * @param request 更新项目场景请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateProjectScene(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long projectSceneId,
            @Valid @RequestBody UpdateProjectSceneRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新项目内场景覆盖, userId: {}, projectId: {}, projectSceneId: {}",
                userId, projectId, projectSceneId);
        projectSceneService.updateProjectScene(userId, projectId, projectSceneId, request);
        return Result.success();
    }

    /**
     * 移除场景引用
     *
     * @param projectId 项目ID
     * @param projectSceneId 项目场景ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeSceneFromProject(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long projectSceneId) {
        Long userId = UserContext.getUserId();
        log.info("移除场景引用, userId: {}, projectId: {}, projectSceneId: {}",
                userId, projectId, projectSceneId);
        projectSceneService.removeSceneFromProject(userId, projectId, projectSceneId);
        return Result.success();
    }

    /**
     * 替换为其他场景
     *
     * @param projectId 项目ID
     * @param projectSceneId 项目场景ID
     * @param request 替换场景请求
     * @return 成功响应
     */
    @PostMapping("/{id}/replace")
    public Result<Void> replaceScene(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long projectSceneId,
            @Valid @RequestBody ReplaceSceneRequest request) {
        Long userId = UserContext.getUserId();
        log.info("替换场景, userId: {}, projectId: {}, projectSceneId: {}, newLibrarySceneId: {}",
                userId, projectId, projectSceneId, request.newLibrarySceneId());
        projectSceneService.replaceScene(userId, projectId, projectSceneId, request);
        return Result.success();
    }
}
