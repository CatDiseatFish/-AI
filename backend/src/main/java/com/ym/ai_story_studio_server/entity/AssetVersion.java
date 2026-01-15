package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产版本表（用于多版本预览/对比/复用）
 */
@Data
@TableName("asset_versions")
public class AssetVersion {

    /**
     * 资产版本ID（历史记录）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 版本号（从1开始，同一asset内唯一）
     */
    private Integer versionNo;

    /**
     * 来源：AI/UPLOAD/IMPORT
     */
    private String source;

    /**
     * 存储提供方：OSS/MINIO/REMOTE等
     */
    private String provider;

    /**
     * 可访问URL（前端展示/下载）
     */
    private String url;

    /**
     * 对象存储Key（OSS/MinIO等，可选）
     */
    private String objectKey;

    /**
     * 生成提示词（可空）
     */
    private String prompt;

    /**
     * 生成参数（JSON）：比例、模型、seed、参考图等
     */
    private String paramsJson;

    /**
     * 状态：READY/FAILED等
     */
    private String status;

    /**
     * 创建人用户ID（可空）
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
