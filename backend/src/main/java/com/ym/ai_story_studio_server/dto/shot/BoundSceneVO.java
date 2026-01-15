package com.ym.ai_story_studio_server.dto.shot;

/**
 * 绑定场景响应VO
 *
 * <p>表示一个与分镜绑定的场景信息
 *
 * @param bindingId 绑定记录ID,用于解绑操作
 * @param sceneId 场景ID
 * @param sceneName 场景名称
 * @param thumbnailUrl 场景缩略图URL,可为null
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record BoundSceneVO(
    Long bindingId,
    Long sceneId,
    String sceneName,
    String thumbnailUrl
) {}
