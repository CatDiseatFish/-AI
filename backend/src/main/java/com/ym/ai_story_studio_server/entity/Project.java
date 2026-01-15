package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目表（作品）
 */
@Data
@TableName("projects")
public class Project {

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 所属文件夹ID（可为空表示未归类）
     */
    private Long folderId;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 画幅比例：如16:9/9:16/21:9等
     */
    private String aspectRatio;

    /**
     * 风格标识：如cinematic/anime等（自定义）
     */
    private String styleCode;

    /**
     * 时代设置（如：现代/古代/未来）- V2新增字段
     */
    private String eraSetting;

    /**
     * 输入的小说/剧本文本原文
     */
    private String rawText;

    /**
     * 项目状态：DRAFT/PROCESSING/READY等
     */
    private String status;

    /**
     * 模型配置（JSON）：语言模型/生图模型/生视频模型等
     */
    private String modelConfigJson;

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
