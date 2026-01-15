package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全局场景库（跨项目复用的场景主档）
 */
@Data
@TableName("scene_library")
public class SceneLibrary {

    /**
     * 全局场景ID（账号级）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 所属分类ID（可空）
     */
    private Long categoryId;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 场景描述/提示词（主档）
     */
    private String description;

    /**
     * 场景缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 软删除时间
     */
    private LocalDateTime deletedAt;
}
