package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前选用版本引用（用于"当前展示/导出/生成视频拼接"）
 */
@Data
@TableName("asset_refs")
public class AssetRef {

    /**
     * 资产引用ID（当前选用）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 引用类型：LIB_CHAR_CURRENT/PCHAR_CURRENT/SCENE_CURRENT/SHOT_IMG_CURRENT/SHOT_VIDEO_CURRENT等
     */
    private String refType;

    /**
     * 引用对象ID（随 ref_type 变化）
     */
    private Long refOwnerId;

    /**
     * 当前选用的资产版本ID
     */
    private Long assetVersionId;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
