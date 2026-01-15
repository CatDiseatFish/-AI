package com.ym.ai_story_studio_server.dto.asset;

import java.time.LocalDateTime;

/**
 * 资产版本响应VO
 *
 * <p>用于返回资产版本详细信息,包括版本号、来源、存储信息等
 *
 * @param id 版本ID
 * @param assetId 资产ID
 * @param versionNo 版本号(从1开始递增)
 * @param source 来源:AI(AI生成)/UPLOAD(用户上传)/IMPORT(导入)
 * @param provider 存储提供方:OSS(阿里云OSS)/MINIO(MinIO)
 * @param url 可访问URL(前端展示/下载使用)
 * @param objectKey 对象存储Key(OSS/MinIO内部标识,可选)
 * @param prompt 生成提示词(仅AI生成时有值,可选)
 * @param paramsJson 生成参数JSON(包含模型、比例、seed等参数,可选)
 * @param status 状态:READY(就绪)/FAILED(失败)
 * @param isCurrent 是否为当前版本(true表示当前正在使用的版本)
 * @param createdBy 创建人用户ID(AI生成为null,用户上传为用户ID)
 * @param createdAt 创建时间
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record AssetVersionVO(
        Long id,
        Long assetId,
        Integer versionNo,
        String source,
        String provider,
        String url,
        String objectKey,
        String prompt,
        String paramsJson,
        String status,
        Boolean isCurrent,
        Long createdBy,
        LocalDateTime createdAt
) {}
