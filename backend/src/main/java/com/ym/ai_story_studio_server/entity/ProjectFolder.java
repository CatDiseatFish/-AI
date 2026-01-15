package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目文件夹（用于首页分类）
 */
@Data
@TableName("project_folders")
public class ProjectFolder {

    /**
     * 文件夹ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 排序值（越小越靠前）
     */
    private Integer sortOrder;

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
     * 软删除时间（非空表示已删除）
     */
    private LocalDateTime deletedAt;
}
