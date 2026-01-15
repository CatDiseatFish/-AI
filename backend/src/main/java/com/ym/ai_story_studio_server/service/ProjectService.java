package com.ym.ai_story_studio_server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.dto.project.CreateProjectRequest;
import com.ym.ai_story_studio_server.dto.project.ProjectQueryRequest;
import com.ym.ai_story_studio_server.dto.project.ProjectVO;
import com.ym.ai_story_studio_server.dto.project.UpdateProjectRequest;

/**
 * 项目服务接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface ProjectService {

    /**
     * 分页查询项目列表（支持搜索和筛选）
     *
     * @param userId 用户ID
     * @param request 查询请求
     * @return 分页结果
     */
    Page<ProjectVO> getProjectPage(Long userId, ProjectQueryRequest request);

    /**
     * 获取项目详情
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 项目详情
     */
    ProjectVO getProjectDetail(Long userId, Long projectId);

    /**
     * 创建项目
     *
     * @param userId 用户ID
     * @param request 创建请求
     * @return 创建的项目
     */
    ProjectVO createProject(Long userId, CreateProjectRequest request);

    /**
     * 更新项目
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param request 更新请求
     */
    void updateProject(Long userId, Long projectId, UpdateProjectRequest request);

    /**
     * 删除项目（软删除）
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     */
    void deleteProject(Long userId, Long projectId);
}
