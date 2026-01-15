package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.scene.AddSceneToProjectRequest;
import com.ym.ai_story_studio_server.dto.scene.ProjectSceneVO;
import com.ym.ai_story_studio_server.dto.scene.ReplaceSceneRequest;
import com.ym.ai_story_studio_server.dto.scene.UpdateProjectSceneRequest;

import java.util.List;

/**
 * 项目场景服务接口
 *
 * <p>提供项目场景管理功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface ProjectSceneService {

    /**
     * 获取项目场景列表
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @return 项目场景VO列表
     */
    List<ProjectSceneVO> getProjectSceneList(Long userId, Long projectId);

    /**
     * 引用场景到项目
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param request 引用场景请求
     * @return 项目场景VO
     */
    ProjectSceneVO addSceneToProject(Long userId, Long projectId, AddSceneToProjectRequest request);

    /**
     * 更新项目内场景覆盖
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param projectSceneId 项目场景ID
     * @param request 更新项目场景请求
     */
    void updateProjectScene(Long userId, Long projectId, Long projectSceneId, UpdateProjectSceneRequest request);

    /**
     * 移除场景引用
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param projectSceneId 项目场景ID
     */
    void removeSceneFromProject(Long userId, Long projectId, Long projectSceneId);

    /**
     * 替换为其他场景
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param projectSceneId 项目场景ID
     * @param request 替换场景请求
     */
    void replaceScene(Long userId, Long projectId, Long projectSceneId, ReplaceSceneRequest request);
}
