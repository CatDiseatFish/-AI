package com.ym.ai_story_studio_server.dto.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新文件夹请求（重命名）
 *
 * @param name 新的文件夹名称
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record UpdateFolderRequest(
        /**
         * 新的文件夹名称
         */
        @NotBlank(message = "文件夹名称不能为空")
        @Size(min = 1, max = 50, message = "文件夹名称长度必须在1-50个字符之间")
        String name
) {
}
