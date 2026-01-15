package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.prop.AddPropToProjectRequest;
import com.ym.ai_story_studio_server.dto.prop.ProjectPropVO;
import com.ym.ai_story_studio_server.dto.prop.UpdateProjectPropRequest;

import java.util.List;

/**
 * 项目道具服务接口
 */
public interface ProjectPropService {

    /**
     * 获取项目道具列表
     *
     * @param projectId 项目ID
     * @return 项目道具VO列表
     */
    List<ProjectPropVO> getProjectProps(Long projectId);

    /**
     * 添加道具到项目
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param request 添加道具请求
     * @return 项目道具VO
     */
    ProjectPropVO addPropToProject(Long userId, Long projectId, AddPropToProjectRequest request);

    /**
     * 更新项目道具
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param propId 项目道具ID
     * @param request 更新请求
     */
    void updateProjectProp(Long userId, Long projectId, Long propId, UpdateProjectPropRequest request);

    /**
     * 从项目中移除道具
     *
     * @param userId 当前用户ID
     * @param projectId 项目ID
     * @param propId 项目道具ID
     */
    void removeFromProject(Long userId, Long projectId, Long propId);
}
