package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.character.AddCharacterToProjectRequest;
import com.ym.ai_story_studio_server.dto.character.CreateProjectCharacterRequest;
import com.ym.ai_story_studio_server.dto.character.ProjectCharacterVO;
import com.ym.ai_story_studio_server.dto.character.ReplaceCharacterRequest;
import com.ym.ai_story_studio_server.dto.character.UpdateProjectCharacterRequest;
import com.ym.ai_story_studio_server.service.ProjectCharacterService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目角色控制器
 *
 * <p>处理项目角色相关接口,需要JWT认证和项目权限验证
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/characters")
@RequiredArgsConstructor
public class ProjectCharacterController {

    private final ProjectCharacterService projectCharacterService;

    /**
     * 获取项目角色列表
     *
     * @param projectId 项目ID
     * @return 项目角色列表响应
     */
    @GetMapping
    public Result<List<ProjectCharacterVO>> getProjectCharacterList(@PathVariable("projectId") Long projectId) {
        Long userId = UserContext.getUserId();
        log.info("获取项目角色列表, userId: {}, projectId: {}", userId, projectId);
        List<ProjectCharacterVO> characters = projectCharacterService.getProjectCharacterList(userId, projectId);
        return Result.success(characters);
    }

    /**
     * 引用角色到项目
     *
     * @param projectId 项目ID
     * @param request 引用角色请求
     * @return 项目角色VO响应
     */
    @PostMapping
    public Result<ProjectCharacterVO> addCharacterToProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody AddCharacterToProjectRequest request) {
        Long userId = UserContext.getUserId();
        log.info("引用角色到项目, userId: {}, projectId: {}, libraryCharacterId: {}",
                userId, projectId, request.libraryCharacterId());
        ProjectCharacterVO character = projectCharacterService.addCharacterToProject(userId, projectId, request);
        return Result.success(character);
    }

    /**
     * 创建项目内自定义角色（不加入角色库）
     */
    @PostMapping("/custom")
    public Result<ProjectCharacterVO> createCustomCharacter(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody CreateProjectCharacterRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建项目内自定义角色, userId: {}, projectId: {}", userId, projectId);
        ProjectCharacterVO character = projectCharacterService.createCustomCharacter(userId, projectId, request);
        return Result.success(character);
    }

    /**
     * 更新项目内角色覆盖
     *
     * @param projectId 项目ID
     * @param projectCharacterId 项目角色ID
     * @param request 更新项目角色请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateProjectCharacter(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long projectCharacterId,
            @Valid @RequestBody UpdateProjectCharacterRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新项目内角色覆盖, userId: {}, projectId: {}, projectCharacterId: {}",
                userId, projectId, projectCharacterId);
        projectCharacterService.updateProjectCharacter(userId, projectId, projectCharacterId, request);
        return Result.success();
    }

    /**
     * 移除角色引用
     *
     * @param projectId 项目ID
     * @param projectCharacterId 项目角色ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeCharacterFromProject(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long projectCharacterId) {
        Long userId = UserContext.getUserId();
        log.info("移除角色引用, userId: {}, projectId: {}, projectCharacterId: {}",
                userId, projectId, projectCharacterId);
        projectCharacterService.removeCharacterFromProject(userId, projectId, projectCharacterId);
        return Result.success();
    }

    /**
     * 替换为其他角色
     *
     * @param projectId 项目ID
     * @param projectCharacterId 项目角色ID
     * @param request 替换角色请求
     * @return 成功响应
     */
    @PostMapping("/{id}/replace")
    public Result<Void> replaceCharacter(
            @PathVariable("projectId") Long projectId,
            @PathVariable("id") Long projectCharacterId,
            @Valid @RequestBody ReplaceCharacterRequest request) {
        Long userId = UserContext.getUserId();
        log.info("替换角色, userId: {}, projectId: {}, projectCharacterId: {}, newLibraryCharacterId: {}",
                userId, projectId, projectCharacterId, request.newLibraryCharacterId());
        projectCharacterService.replaceCharacter(userId, projectId, projectCharacterId, request);
        return Result.success();
    }
}
