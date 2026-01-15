package com.ym.ai_story_studio_server.dto.shot;

/**
 * 绑定道具响应VO
 *
 * <p>表示一个与分镜绑定的项目道具信息
 *
 * @param bindingId 绑定记录ID,用于解绑操作
 * @param propId 项目道具ID
 * @param propName 道具名称
 * @param thumbnailUrl 道具缩略图URL,可为null
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record BoundPropVO(
    Long bindingId,
    Long propId,
    String propName,
    String thumbnailUrl
) {}
