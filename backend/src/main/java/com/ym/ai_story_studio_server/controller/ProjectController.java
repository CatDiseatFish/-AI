package com.ym.ai_story_studio_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.project.CreateProjectRequest;
import com.ym.ai_story_studio_server.dto.project.ProjectQueryRequest;
import com.ym.ai_story_studio_server.dto.project.ProjectVO;
import com.ym.ai_story_studio_server.dto.project.UpdateProjectRequest;
import com.ym.ai_story_studio_server.service.ProjectService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 项目控制器
 *
 * <p>处理项目的增删改查相关接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 分页查询项目列表
     * <p>支持搜索和筛选
     * <p>需要JWT认证
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<ProjectVO>> getProjectPage(ProjectQueryRequest request) {
        Long userId = UserContext.getUserId();
        log.info("收到查询项目列表请求: userId={}, request={}", userId, request);
        Page<ProjectVO> page = projectService.getProjectPage(userId, request);
        return Result.success(page);
    }

    /**
     * 获取项目详情
     * <p>需要JWT认证
     *
     * @param projectId 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public Result<ProjectVO> getProjectDetail(@PathVariable("id") Long projectId) {
        Long userId = UserContext.getUserId();
        log.info("收到获取项目详情请求: userId={}, projectId={}", userId, projectId);
        ProjectVO project = projectService.getProjectDetail(userId, projectId);
        return Result.success(project);
    }

    /**
     * 创建项目
     * <p>需要JWT认证
     *
     * @param request 创建请求
     * @return 创建的项目
     */
    @PostMapping
    public Result<ProjectVO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        Long userId = UserContext.getUserId();
        log.info("收到创建项目请求: userId={}, name={}", userId, request.name());
        ProjectVO project = projectService.createProject(userId, request);
        return Result.success("项目创建成功", project);
    }

    /**
     * 更新项目
     * <p>需要JWT认证
     *
     * @param projectId 项目ID
     * @param request 更新请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateProject(
            @PathVariable("id") Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        Long userId = UserContext.getUserId();
        log.info("收到更新项目请求: userId={}, projectId={}", userId, projectId);
        projectService.updateProject(userId, projectId, request);
        return Result.success();
    }

    /**
     * 删除项目（软删除）
     * <p>需要JWT认证
     *
     * @param projectId 项目ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable("id") Long projectId) {
        Long userId = UserContext.getUserId();
        log.info("收到删除项目请求: userId={}, projectId={}", userId, projectId);
        projectService.deleteProject(userId, projectId);
        return Result.success();
    }
}
