package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分镜与角色/场景的绑定关系（多对多）
 */
@Data
@TableName("shot_bindings")
public class ShotBinding {

    /**
     * 分镜绑定ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分镜ID
     */
    private Long shotId;

    /**
     * 绑定类型：PCHAR(项目角色)/PSCENE(场景)/PPROP(道具)
     */
    private String bindType;

    /**
     * 绑定对象ID：project_characters.id、project_scenes.id 或 project_props.id
     */
    private Long bindId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
