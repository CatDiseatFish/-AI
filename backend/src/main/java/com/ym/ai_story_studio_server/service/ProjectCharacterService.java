package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.character.AddCharacterToProjectRequest;
import com.ym.ai_story_studio_server.dto.character.ProjectCharacterVO;
import com.ym.ai_story_studio_server.dto.character.ReplaceCharacterRequest;
import com.ym.ai_story_studio_server.dto.character.UpdateProjectCharacterRequest;

import java.util.List;

/**
 * 项目角色服务接口
 *
 * <p>提供项目内角色引用、覆盖、替换功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface ProjectCharacterService {

    /**
     * 获取项目角色列表
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @return 项目角色VO列表
     */
    List<ProjectCharacterVO> getProjectCharacterList(Long userId, Long projectId);

    /**
     * 引用角色到项目
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param request 引用角色请求
     * @return 项目角色VO
     */
    ProjectCharacterVO addCharacterToProject(Long userId, Long projectId, AddCharacterToProjectRequest request);

    /**
     * 更新项目内角色覆盖
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param projectCharacterId 项目角色ID
     * @param request 更新项目角色请求
     */
    void updateProjectCharacter(Long userId, Long projectId, Long projectCharacterId, UpdateProjectCharacterRequest request);

    /**
     * 移除角色引用
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param projectCharacterId 项目角色ID
     */
    void removeCharacterFromProject(Long userId, Long projectId, Long projectCharacterId);

    /**
     * 替换为其他角色
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param projectCharacterId 项目角色ID
     * @param request 替换角色请求
     */
    void replaceCharacter(Long userId, Long projectId, Long projectCharacterId, ReplaceCharacterRequest request);
}
