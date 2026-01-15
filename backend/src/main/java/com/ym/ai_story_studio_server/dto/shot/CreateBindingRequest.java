package com.ym.ai_story_studio_server.dto.shot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 创建绑定关系请求DTO
 *
 * <p>用于在分镜与角色/场景/道具之间创建绑定关系
 *
 * @param bindType 绑定类型,必须是"PCHAR"(项目角色)、"PSCENE"(项目场景)或"PPROP"(项目道具)
 * @param bindId 绑定对象ID,对应project_characters.id、project_scenes.id或project_props.id,不能为空
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateBindingRequest(
    @NotBlank(message = "绑定类型不能为空")
    @Pattern(regexp = "PCHAR|PSCENE|PPROP", message = "绑定类型必须是PCHAR、PSCENE或PPROP")
    String bindType,

    @NotNull(message = "绑定对象ID不能为空")
    Long bindId
) {}
