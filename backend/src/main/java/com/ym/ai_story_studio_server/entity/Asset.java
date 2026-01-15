package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产主表（一个资产对应多个版本）
 */
@Data
@TableName("assets")
public class Asset {

    /**
     * 资产ID（逻辑资产）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID
     */
    private Long projectId;

     /**
     * 资产类型：CHAR_IMG/SCENE_IMG/SHOT_IMG/VIDEO/REF_IMG/COMPOSITE_IMG等
     */
    private String assetType;

    /**
     * 归属对象类型：LIB_CHAR/PCHAR/LIB_SCENE/PSCENE/SHOT/PROJECT
     */
    private String ownerType;

    /**
     * 归属对象ID（随 owner_type 变化）
     */
    private Long ownerId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
