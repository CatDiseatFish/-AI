package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.folder.CreateFolderRequest;
import com.ym.ai_story_studio_server.dto.folder.FolderVO;
import com.ym.ai_story_studio_server.dto.folder.UpdateFolderRequest;
import com.ym.ai_story_studio_server.entity.ProjectFolder;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.ProjectFolderMapper;
import com.ym.ai_story_studio_server.mapper.ProjectMapper;
import com.ym.ai_story_studio_server.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件夹服务实现类
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final ProjectFolderMapper folderMapper;
    private final ProjectMapper projectMapper;

    @Override
    public List<FolderVO> getFolderList(Long userId) {
        log.debug("查询文件夹列表: userId={}", userId);

        // 1. 查询用户的文件夹（未删除的）
        LambdaQueryWrapper<ProjectFolder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectFolder::getUserId, userId)
                .isNull(ProjectFolder::getDeletedAt)
                .orderByAsc(ProjectFolder::getSortOrder);

        List<ProjectFolder> folders = folderMapper.selectList(queryWrapper);

        // 2. 转换为VO并统计项目数量
        return folders.stream().map(folder -> {
            // 统计该文件夹下的项目数量
            Long count = projectMapper.selectCount(
                    new LambdaQueryWrapper<com.ym.ai_story_studio_server.entity.Project>()
                            .eq(com.ym.ai_story_studio_server.entity.Project::getFolderId, folder.getId())
                            .isNull(com.ym.ai_story_studio_server.entity.Project::getDeletedAt)
            );

            return new FolderVO(
                    folder.getId(),
                    folder.getName(),
                    folder.getSortOrder(),
                    count.intValue(),
                    folder.getCreatedAt(),
                    folder.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FolderVO createFolder(Long userId, CreateFolderRequest request) {
        log.info("创建文件夹: userId={}, name={}", userId, request.name());

        // 1. 检查名称是否重复
        Long count = folderMapper.selectCount(
                new LambdaQueryWrapper<ProjectFolder>()
                        .eq(ProjectFolder::getUserId, userId)
                        .eq(ProjectFolder::getName, request.name())
                        .isNull(ProjectFolder::getDeletedAt)
        );

        if (count > 0) {
            log.warn("文件夹名称已存在: userId={}, name={}", userId, request.name());
            throw new BusinessException(ResultCode.FOLDER_NAME_DUPLICATE);
        }

        // 2. 获取当前用户最大的排序值
        LambdaQueryWrapper<ProjectFolder> maxWrapper = new LambdaQueryWrapper<>();
        maxWrapper.eq(ProjectFolder::getUserId, userId)
                .isNull(ProjectFolder::getDeletedAt)
                .orderByDesc(ProjectFolder::getSortOrder)
                .last("LIMIT 1");

        ProjectFolder lastFolder = folderMapper.selectOne(maxWrapper);
        int nextSortOrder = (lastFolder != null) ? lastFolder.getSortOrder() + 1 : 0;

        // 3. 创建文件夹实体
        ProjectFolder folder = new ProjectFolder();
        folder.setUserId(userId);
        folder.setName(request.name());
        folder.setSortOrder(nextSortOrder);

        // 4. 保存到数据库
        folderMapper.insert(folder);

        log.info("文件夹创建成功: id={}, name={}", folder.getId(), folder.getName());

        return new FolderVO(
                folder.getId(),
                folder.getName(),
                folder.getSortOrder(),
                0,
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void updateFolder(Long userId, Long folderId, UpdateFolderRequest request) {
        log.info("更新文件夹: userId={}, folderId={}, newName={}", userId, folderId, request.name());

        // 1. 查询文件夹是否存在
        ProjectFolder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getDeletedAt() != null) {
            log.warn("文件夹不存在: folderId={}", folderId);
            throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
        }

        // 2. 验证是否属于当前用户
        if (!folder.getUserId().equals(userId)) {
            log.warn("无权限访问该文件夹: userId={}, folderId={}", userId, folderId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 检查新名称是否与其他文件夹重复
        if (!folder.getName().equals(request.name())) {
            Long count = folderMapper.selectCount(
                    new LambdaQueryWrapper<ProjectFolder>()
                            .eq(ProjectFolder::getUserId, userId)
                            .eq(ProjectFolder::getName, request.name())
                            .isNull(ProjectFolder::getDeletedAt)
                            .ne(ProjectFolder::getId, folderId)
            );

            if (count > 0) {
                log.warn("文件夹名称已存在: userId={}, name={}", userId, request.name());
                throw new BusinessException(ResultCode.FOLDER_NAME_DUPLICATE);
            }
        }

        // 4. 更新文件夹名称
        folder.setName(request.name());
        folderMapper.updateById(folder);

        log.info("文件夹更新成功: folderId={}, newName={}", folderId, request.name());
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        log.info("删除文件夹: userId={}, folderId={}", userId, folderId);

        // 1. 查询文件夹是否存在
        ProjectFolder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getDeletedAt() != null) {
            log.warn("文件夹不存在: folderId={}", folderId);
            throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
        }

        // 2. 验证是否属于当前用户
        if (!folder.getUserId().equals(userId)) {
            log.warn("无权限访问该文件夹: userId={}, folderId={}", userId, folderId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 检查文件夹下是否有项目
        Long projectCount = projectMapper.selectCount(
                new LambdaQueryWrapper<com.ym.ai_story_studio_server.entity.Project>()
                        .eq(com.ym.ai_story_studio_server.entity.Project::getFolderId, folderId)
                        .isNull(com.ym.ai_story_studio_server.entity.Project::getDeletedAt)
        );

        if (projectCount > 0) {
            log.warn("文件夹下还有项目，无法删除: folderId={}, projectCount={}", folderId, projectCount);
            throw new BusinessException(ResultCode.PARAM_INVALID, "文件夹下还有项目，请先移动或删除项目");
        }

        // 4. 软删除文件夹
        folder.setDeletedAt(LocalDateTime.now());
        folderMapper.updateById(folder);

        log.info("文件夹删除成功: folderId={}", folderId);
    }
}
