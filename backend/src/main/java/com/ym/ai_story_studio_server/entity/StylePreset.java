package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风格预设表
 * 用于存储系统预设的图片/视频风格（赛璐璐、皮克斯、迪士尼3D等）
 */
@Data
@TableName("style_presets")
public class StylePreset {

    /**
     * 风格预设ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 风格代码（唯一标识）
     * 如：celluloid、urban_korean、pixar、disney3d、ghibli、anime
     */
    private String code;

    /**
     * 风格名称
     * 如：赛璐璐、都市韩漫、皮克斯、迪士尼3D、宫崎骏、日漫
     */
    private String name;

    /**
     * 预览缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 风格提示词前缀（生图时自动拼接到用户提示词前面）
     */
    private String promptPrefix;

    /**
     * 排序值（越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 是否启用：1启用，0禁用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
