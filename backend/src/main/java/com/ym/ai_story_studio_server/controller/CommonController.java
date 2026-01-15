package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.service.StorageService;
import com.ym.ai_story_studio_server.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用文件上传接口
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommonController {

    private final StorageService storageService;

    // 最大文件大小: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    /**
     * 通用文件上传接口
     * <p>支持图片、视频、音频上传
     *
     * @param file 文件对象
     * @param type 文件类型 (image/video/audio)
     * @return 文件URL
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "image") String type
    ) {
        Long userId = UserContext.getUserId();
        log.info("收到文件上传请求: userId={}, fileName={}, size={}, type={}",
                userId, file.getOriginalFilename(), file.getSize(), type);

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "文件大小不能超过50MB");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "无法识别文件类型");
        }

        // 根据type验证content-type
        boolean validType = switch (type) {
            case "image" -> contentType.startsWith("image/");
            case "video" -> contentType.startsWith("video/");
            case "audio" -> contentType.startsWith("audio/");
            default -> false;
        };

        if (!validType) {
            throw new BusinessException(ResultCode.PARAM_INVALID, 
                    String.format("文件类型不匹配: 期望%s,实际%s", type, contentType));
        }

        try {
            String path = String.format("%s/%s/%s", type, userId, file.getOriginalFilename());
            String url = storageService.upload(
                    file.getInputStream(),
                    path,
                    contentType
            );
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            
            log.info("文件上传成功: userId={}, url={}", userId, url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败: userId={}", userId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }
    }
}
