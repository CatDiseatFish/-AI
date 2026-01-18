package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.project.CreateProjectRequest;
import com.ym.ai_story_studio_server.dto.project.ProjectQueryRequest;
import com.ym.ai_story_studio_server.dto.project.ProjectVO;
import com.ym.ai_story_studio_server.dto.project.UpdateProjectRequest;
import com.ym.ai_story_studio_server.entity.Project;
import com.ym.ai_story_studio_server.entity.ProjectFolder;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.ProjectFolderMapper;
import com.ym.ai_story_studio_server.mapper.ProjectMapper;
import com.ym.ai_story_studio_server.mapper.StoryboardShotMapper;
import com.ym.ai_story_studio_server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 项目服务实现类
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectFolderMapper folderMapper;
    private final StoryboardShotMapper shotMapper;

    @Override
    public Page<ProjectVO> getProjectPage(Long userId, ProjectQueryRequest request) {
        log.debug("查询项目列表: userId={}, request={}", userId, request);

        // 1. 构建分页对象
        Page<Project> page = new Page<>(request.page(), request.size());

        // 2. 构建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Project::getUserId, userId)
                .isNull(Project::getDeletedAt);

        // 文件夹筛选
        if (request.folderId() != null) {
            queryWrapper.eq(Project::getFolderId, request.folderId());
        }

        // 关键词搜索（项目名称）
        if (StringUtils.hasText(request.keyword())) {
            queryWrapper.like(Project::getName, request.keyword());
        }

        // 画幅比例筛选
        if (StringUtils.hasText(request.aspectRatio())) {
            queryWrapper.eq(Project::getAspectRatio, request.aspectRatio());
        }

        // 风格筛选
        if (StringUtils.hasText(request.styleCode())) {
            queryWrapper.eq(Project::getStyleCode, request.styleCode());
        }

        // 状态筛选
        if (StringUtils.hasText(request.status())) {
            queryWrapper.eq(Project::getStatus, request.status());
        }

        // 按更新时间降序排序
        queryWrapper.orderByDesc(Project::getUpdatedAt);

        // 3. 执行分页查询
        Page<Project> projectPage = projectMapper.selectPage(page, queryWrapper);

        // 4. 转换为VO
        return (Page<ProjectVO>) projectPage.convert(project -> {
            // 查询文件夹名称
            String folderName = null;
            if (project.getFolderId() != null) {
                ProjectFolder folder = folderMapper.selectById(project.getFolderId());
                if (folder != null && folder.getDeletedAt() == null) {
                    folderName = folder.getName();
                }
            }

            // 统计分镜数量
            Long shotCount = shotMapper.selectCount(
                    new LambdaQueryWrapper<com.ym.ai_story_studio_server.entity.StoryboardShot>()
                            .eq(com.ym.ai_story_studio_server.entity.StoryboardShot::getProjectId, project.getId())
            );

            return new ProjectVO(
                    project.getId(),
                    project.getFolderId(),
                    folderName,
                    project.getName(),
                    project.getAspectRatio(),
                    project.getStyleCode(),
                    project.getEraSetting(),
                    project.getStatus(),
                    project.getModelConfigJson(),
                    shotCount.intValue(),
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
        });
    }

    @Override
    public ProjectVO getProjectDetail(Long userId, Long projectId) {
        log.debug("查询项目详情: userId={}, projectId={}", userId, projectId);

        // 1. 查询项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeletedAt() != null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 2. 验证是否属于当前用户
        if (!project.getUserId().equals(userId)) {
            log.warn("无权限访问该项目: userId={}, projectId={}", userId, projectId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 查询文件夹名称
        String folderName = null;
        if (project.getFolderId() != null) {
            ProjectFolder folder = folderMapper.selectById(project.getFolderId());
            if (folder != null && folder.getDeletedAt() == null) {
                folderName = folder.getName();
            }
        }

        // 4. 统计分镜数量
        Long shotCount = shotMapper.selectCount(
                new LambdaQueryWrapper<com.ym.ai_story_studio_server.entity.StoryboardShot>()
                        .eq(com.ym.ai_story_studio_server.entity.StoryboardShot::getProjectId, project.getId())
        );

        return new ProjectVO(
                project.getId(),
                project.getFolderId(),
                folderName,
                project.getName(),
                project.getAspectRatio(),
                project.getStyleCode(),
                project.getEraSetting(),
                project.getStatus(),
                project.getModelConfigJson(),
                shotCount.intValue(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public ProjectVO createProject(Long userId, CreateProjectRequest request) {
        log.info("创建项目: userId={}, name={}", userId, request.name());

        // 1. 如果指定了文件夹，验证文件夹是否存在
        if (request.folderId() != null) {
            ProjectFolder folder = folderMapper.selectById(request.folderId());
            if (folder == null || folder.getDeletedAt() != null) {
                log.warn("文件夹不存在: folderId={}", request.folderId());
                throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
            }
            if (!folder.getUserId().equals(userId)) {
                log.warn("无权限访问该文件夹: userId={}, folderId={}", userId, request.folderId());
                throw new BusinessException(ResultCode.ACCESS_DENIED);
            }
        }

        // 2. 创建项目实体
        Project project = new Project();
        project.setUserId(userId);
        project.setName(request.name());
        project.setFolderId(request.folderId());
        project.setAspectRatio(request.aspectRatio());
        project.setStyleCode(request.styleCode());
        project.setEraSetting(request.eraSetting());
        project.setRawText(request.rawText());
        project.setStatus("DRAFT"); // 默认状态为草稿

        // 3. 保存到数据库
        projectMapper.insert(project);

        log.info("项目创建成功: id={}, name={}", project.getId(), project.getName());

        return new ProjectVO(
                project.getId(),
                project.getFolderId(),
                null,
                project.getName(),
                project.getAspectRatio(),
                project.getStyleCode(),
                project.getEraSetting(),
                project.getStatus(),
                project.getModelConfigJson(),
                0,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void updateProject(Long userId, Long projectId, UpdateProjectRequest request) {
        log.info("更新项目: userId={}, projectId={}", userId, projectId);

        // 1. 查询项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeletedAt() != null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 2. 验证是否属于当前用户
        if (!project.getUserId().equals(userId)) {
            log.warn("无权限访问该项目: userId={}, projectId={}", userId, projectId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 如果指定了新文件夹，验证文件夹是否存在
        if (request.folderId() != null) {
            ProjectFolder folder = folderMapper.selectById(request.folderId());
            if (folder == null || folder.getDeletedAt() != null) {
                log.warn("文件夹不存在: folderId={}", request.folderId());
                throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
            }
            if (!folder.getUserId().equals(userId)) {
                log.warn("无权限访问该文件夹: userId={}, folderId={}", userId, request.folderId());
                throw new BusinessException(ResultCode.ACCESS_DENIED);
            }
        }

        // 4. 更新项目信息
        boolean needUpdate = false;

        if (StringUtils.hasText(request.name()) && !project.getName().equals(request.name())) {
            project.setName(request.name());
            needUpdate = true;
            log.info("更新项目名称: projectId={}, newName={}", projectId, request.name());
        }

        if (request.folderId() != null && !Objects.equals(project.getFolderId(), request.folderId())) {
            project.setFolderId(request.folderId());
            needUpdate = true;
            log.info("更新项目文件夹: projectId={}, newFolderId={}", projectId, request.folderId());
        }

        if (StringUtils.hasText(request.aspectRatio())) {
            project.setAspectRatio(request.aspectRatio());
            needUpdate = true;
        }

        if (request.styleCode() != null) {
            project.setStyleCode(request.styleCode());
            needUpdate = true;
        }

        if (request.eraSetting() != null) {
            project.setEraSetting(request.eraSetting());
            needUpdate = true;
        }

        if (request.rawText() != null) {
            project.setRawText(request.rawText());
            needUpdate = true;
        }

        if (request.modelConfigJson() != null) {
            project.setModelConfigJson(request.modelConfigJson());
            needUpdate = true;
        }

        // 5. 保存到数据库
        if (needUpdate) {
            projectMapper.updateById(project);
            log.info("项目更新成功: projectId={}", projectId);
        } else {
            log.info("无需更新项目: projectId={}", projectId);
        }
    }

    @Override
    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        log.info("删除项目: userId={}, projectId={}", userId, projectId);

        // 1. 查询项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeletedAt() != null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 2. 验证是否属于当前用户
        if (!project.getUserId().equals(userId)) {
            log.warn("无权限访问该项目: userId={}, projectId={}", userId, projectId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 软删除项目
        project.setDeletedAt(LocalDateTime.now());
        projectMapper.updateById(project);

        log.info("项目删除成功: projectId={}", projectId);
    }
}
