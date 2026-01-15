package com.ym.ai_story_studio_server.dto.export;

import jakarta.validation.constraints.NotNull;

/**
 * 导出请求DTO
 *
 * @param exportCharacters 是否导出角色画像
 * @param exportScenes 是否导出场景画像
 * @param exportShotImages 是否导出分镜图
 * @param exportVideos 是否导出视频
 * @param mode 导出模式:CURRENT(仅当前版本)/ALL(包含所有历史版本)
 */
public record ExportRequest(
        @NotNull(message = "exportCharacters不能为空")
        Boolean exportCharacters,

        @NotNull(message = "exportScenes不能为空")
        Boolean exportScenes,

        @NotNull(message = "exportShotImages不能为空")
        Boolean exportShotImages,

        @NotNull(message = "exportVideos不能为空")
        Boolean exportVideos,

        @NotNull(message = "导出模式不能为空")
        String mode
) {}
