package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全局角色库（跨项目复用的角色主档）
 */
@Data
@TableName("character_library")
public class CharacterLibrary {

    /**
     * 全局角色ID（账号级）
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
     * 角色名称
     */
    private String name;

    /**
     * 角色描述/提示词（主档）
     */
    private String description;

    /**
     * 角色缩略图URL
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
