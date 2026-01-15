package com.ym.ai_story_studio_server.dto.shot;

/**
 * 资产状态响应VO
 *
 * <p>表示一个资产(分镜图/视频等)的当前状态信息
 *
 * @param assetId 资产ID,可为null表示资产不存在
 * @param currentVersionId 当前版本ID,可为null
 * @param currentUrl 当前版本的URL,可为null
 * @param status 资产状态:NONE(无资产)/GENERATING(生成中)/READY(已就绪)/FAILED(失败)
 * @param totalVersions 版本总数
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record AssetStatusVO(
    Long assetId,
    Long currentVersionId,
    String currentUrl,
    String status,
    Integer totalVersions
) {}
