package com.ym.ai_story_studio_server.dto.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建文件夹请求
 *
 * @param name 文件夹名称
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateFolderRequest(
        /**
         * 文件夹名称
         */
        @NotBlank(message = "文件夹名称不能为空")
        @Size(min = 1, max = 50, message = "文件夹名称长度必须在1-50个字符之间")
        String name
) {
}
