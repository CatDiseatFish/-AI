package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务子项（批量生成：每个分镜/角色/场景一条）
 */
@Data
@TableName("job_items")
public class JobItem {

    /**
     * 子任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属任务ID
     */
    private Long jobId;

    /**
     * 目标对象类型：LIB_CHAR/PCHAR/LIB_SCENE/PSCENE/SHOT/PROJECT
     */
    private String targetType;

    /**
     * 目标对象ID
     */
    private Long targetId;

    /**
     * 子任务状态：PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED
     */
    private String status;

    /**
     * 输入参数（JSON）：prompt、参考图version_id、数量、比例等
     */
    private String inputJson;

    /**
     * 输出资产版本ID（成功后写入）
     */
    private Long outputAssetVersionId;

    /**
     * 子任务错误信息
     */
    private String errorMessage;

    /**
     * 子任务开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 子任务结束时间
     */
    private LocalDateTime finishedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
