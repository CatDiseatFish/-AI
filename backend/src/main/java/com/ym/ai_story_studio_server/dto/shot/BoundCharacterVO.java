package com.ym.ai_story_studio_server.dto.shot;

/**
 * 绑定角色响应VO
 *
 * <p>表示一个与分镜绑定的项目角色信息
 *
 * @param bindingId 绑定记录ID,用于解绑操作
 * @param characterId 项目角色ID
 * @param characterName 角色名称
 * @param thumbnailUrl 角色缩略图URL,可为null
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record BoundCharacterVO(
    Long bindingId,
    Long characterId,
    String characterName,
    String thumbnailUrl
) {}
