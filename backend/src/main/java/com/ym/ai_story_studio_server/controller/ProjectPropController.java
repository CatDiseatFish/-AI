package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.prop.AddPropToProjectRequest;
import com.ym.ai_story_studio_server.dto.prop.CreateProjectPropRequest;
import com.ym.ai_story_studio_server.dto.prop.ProjectPropVO;
import com.ym.ai_story_studio_server.dto.prop.UpdateProjectPropRequest;
import com.ym.ai_story_studio_server.service.ProjectPropService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目道具控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/props")
@RequiredArgsConstructor
public class ProjectPropController {

    private final ProjectPropService projectPropService;

    /**
     * 获取项目道具列表
     */
    @GetMapping
    public Result<List<ProjectPropVO>> getProjectProps(@PathVariable Long projectId) {
        log.info("获取项目道具列表, projectId: {}", projectId);
        List<ProjectPropVO> props = projectPropService.getProjectProps(projectId);
        return Result.success(props);
    }

    /**
     * 添加道具到项目
     */
    @PostMapping
    public Result<ProjectPropVO> addPropToProject(
            @PathVariable Long projectId,
            @Valid @RequestBody AddPropToProjectRequest request) {
        Long userId = UserContext.getUserId();
        log.info("添加道具到项目, userId: {}, projectId: {}", userId, projectId);
        ProjectPropVO prop = projectPropService.addPropToProject(userId, projectId, request);
        return Result.success(prop);
    }

    /**
     * 创建项目内自定义道具（不加入道具库）
     */
    @PostMapping("/custom")
    public Result<ProjectPropVO> createCustomProp(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectPropRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建项目内自定义道具, userId: {}, projectId: {}", userId, projectId);
        ProjectPropVO prop = projectPropService.createCustomProp(userId, projectId, request);
        return Result.success(prop);
    }

    /**
     * 更新项目道具
     */
    @PutMapping("/{propId}")
    public Result<Void> updateProjectProp(
            @PathVariable Long projectId,
            @PathVariable Long propId,
            @Valid @RequestBody UpdateProjectPropRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新项目道具, userId: {}, projectId: {}, propId: {}", userId, projectId, propId);
        projectPropService.updateProjectProp(userId, projectId, propId, request);
        return Result.success();
    }

    /**
     * 从项目中移除道具
     */
    @DeleteMapping("/{propId}")
    public Result<Void> removeFromProject(
            @PathVariable Long projectId,
            @PathVariable Long propId) {
        Long userId = UserContext.getUserId();
        log.info("从项目移除道具, userId: {}, projectId: {}, propId: {}", userId, projectId, propId);
        projectPropService.removeFromProject(userId, projectId, propId);
        return Result.success();
    }
}
