package com.ym.ai_story_studio_server.dto.shot;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分镜响应VO
 *
 * <p>包含分镜的完整信息,包括绑定的角色/场景和资产状态
 *
 * @param id 分镜ID
 * @param shotNo 分镜序号
 * @param scriptText 剧本文本
 * @param characters 绑定的项目角色列表
 * @param scene 绑定的场景(单个),可为null
 * @param shotImage 分镜图资产状态,可为null
 * @param video 视频资产状态,可为null
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ShotVO(
    Long id,
    Integer shotNo,
    String scriptText,
    List<BoundCharacterVO> characters,
    BoundSceneVO scene,
    List<BoundPropVO> props,
    AssetStatusVO shotImage,
    AssetStatusVO video,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
