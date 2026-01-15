package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.folder.CreateFolderRequest;
import com.ym.ai_story_studio_server.dto.folder.FolderVO;
import com.ym.ai_story_studio_server.dto.folder.UpdateFolderRequest;
import com.ym.ai_story_studio_server.service.FolderService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件夹控制器
 *
 * <p>处理文件夹的增删改查相关接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    /**
     * 获取文件夹列表
     * <p>需要JWT认证
     *
     * @return 文件夹列表（按排序值排序）
     */
    @GetMapping
    public Result<List<FolderVO>> getFolderList() {
        Long userId = UserContext.getUserId();
        log.info("收到获取文件夹列表请求: userId={}", userId);
        List<FolderVO> folders = folderService.getFolderList(userId);
        return Result.success(folders);
    }

    /**
     * 创建文件夹
     * <p>需要JWT认证
     *
     * @param request 创建请求
     * @return 创建的文件夹
     */
    @PostMapping
    public Result<FolderVO> createFolder(@Valid @RequestBody CreateFolderRequest request) {
        Long userId = UserContext.getUserId();
        log.info("收到创建文件夹请求: userId={}, name={}", userId, request.name());
        FolderVO folder = folderService.createFolder(userId, request);
        return Result.success("文件夹创建成功", folder);
    }

    /**
     * 更新文件夹（重命名）
     * <p>需要JWT认证
     *
     * @param folderId 文件夹ID
     * @param request 更新请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateFolder(
            @PathVariable("id") Long folderId,
            @Valid @RequestBody UpdateFolderRequest request) {
        Long userId = UserContext.getUserId();
        log.info("收到更新文件夹请求: userId={}, folderId={}, newName={}", userId, folderId, request.name());
        folderService.updateFolder(userId, folderId, request);
        return Result.success();
    }

    /**
     * 删除文件夹（软删除）
     * <p>需要JWT认证
     *
     * @param folderId 文件夹ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFolder(@PathVariable("id") Long folderId) {
        Long userId = UserContext.getUserId();
        log.info("收到删除文件夹请求: userId={}, folderId={}", userId, folderId);
        folderService.deleteFolder(userId, folderId);
        return Result.success();
    }
}
