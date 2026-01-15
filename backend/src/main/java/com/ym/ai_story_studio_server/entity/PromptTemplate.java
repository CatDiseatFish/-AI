package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指令库/提示词模板表
 * 存储系统预设和用户自定义的提示词模板
 */
@Data
@TableName("prompt_templates")
public class PromptTemplate {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID（NULL表示系统预设模板）
     */
    private Long userId;

    /**
     * 类别：CHARACTER（角色）/SCENE（场景）/SHOT（分镜）/VIDEO（视频）/CUSTOM（自定义）
     */
    private String category;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板内容（提示词文本）
     */
    private String content;

    /**
     * 是否系统预设：1是，0否
     */
    private Integer isSystem;

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
}
