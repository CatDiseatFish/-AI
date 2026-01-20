package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.user.UpdateProfileRequest;
import com.ym.ai_story_studio_server.dto.user.UserProfileVO;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.service.StorageService;
import com.ym.ai_story_studio_server.service.UserService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 *
 * <p>处理用户信息查询和更新相关接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final StorageService storageService;

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 获取用户信息
     * <p>需要JWT认证
     *
     * @return 用户信息VO
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        Long userId = UserContext.getUserId();
        log.info("收到获取用户信息请求: userId={}", userId);
        UserProfileVO vo = userService.getUserProfile(userId);
        return Result.success(vo);
    }

    /**
     * 更新用户信息
     * <p>需要JWT认证
     *
     * @param request 更新请求（可选更新昵称和头像）
     * @return 成功响应
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.getUserId();
        log.info("收到更新用户信息请求: userId={}, nickname={}, avatarUrl={}",
                userId, request.nickname(), request.avatarUrl());
        userService.updateProfile(userId, request);
        return Result.success();
    }


    /**
     * 上传用户头像
     * <p>需要JWT认证，限制5MB以内的图片文件
     *
     * @param file 头像图片文件
     * @return 头像URL
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        log.info("收到上传头像请求: userId={}, fileName={}, size={}",
                userId, file.getOriginalFilename(), file.getSize());

        // 校验文件大小
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ResultCode.ONLY_IMAGE_FILES_ALLOWED);
        }

        try {
            String url = storageService.upload(
                    file.getInputStream(),
                    "avatar/" + userId + "/" + file.getOriginalFilename(),
                    contentType
            );
            return Result.success(url);
        } catch (Exception e) {
            log.error("头像上传失败: userId={}", userId, e);
            throw new BusinessException(ResultCode.AVATAR_UPLOAD_FAILED);
        }
    }
}
