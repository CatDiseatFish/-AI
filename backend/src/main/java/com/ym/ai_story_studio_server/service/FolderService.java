package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.folder.CreateFolderRequest;
import com.ym.ai_story_studio_server.dto.folder.FolderVO;
import com.ym.ai_story_studio_server.dto.folder.UpdateFolderRequest;

import java.util.List;

/**
 * 文件夹服务接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface FolderService {

    /**
     * 获取用户的文件夹列表（按排序值排序）
     *
     * @param userId 用户ID
     * @return 文件夹列表
     */
    List<FolderVO> getFolderList(Long userId);

    /**
     * 创建文件夹
     *
     * @param userId 用户ID
     * @param request 创建请求
     * @return 创建的文件夹
     */
    FolderVO createFolder(Long userId, CreateFolderRequest request);

    /**
     * 更新文件夹（重命名）
     *
     * @param userId 用户ID
     * @param folderId 文件夹ID
     * @param request 更新请求
     */
    void updateFolder(Long userId, Long folderId, UpdateFolderRequest request);

    /**
     * 删除文件夹（软删除）
     *
     * @param userId 用户ID
     * @param folderId 文件夹ID
     */
    void deleteFolder(Long userId, Long folderId);
}
