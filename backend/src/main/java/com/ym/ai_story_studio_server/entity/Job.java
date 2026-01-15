package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务主表（用于队列小窗展示与异步处理）
 */
@Data
@TableName("jobs")
public class Job {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发起用户ID
     */
    private Long userId;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 任务类型：PARSE_TEXT/GEN_CHAR_IMG/GEN_SCENE_IMG/GEN_SHOT_IMG/GEN_VIDEO/EXPORT_ZIP
     */
    private String jobType;

    /**
     * 任务状态：PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED
     */
    private String status;

    /**
     * 任务进度：0-100
     */
    private Integer progress;

    /**
     * 子任务总数（批量生成用）
     */
    private Integer totalItems;

    /**
     * 已完成子任务数
     */
    private Integer doneItems;

    /**
     * 开始时间（用于计时展示）
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime finishedAt;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 结果URL（生成的图片/视频URL等）
     */
    private String resultUrl;

    /**
     * 消耗积分
     */
    private Integer costPoints;

    /**
     * 扩展参数（JSON）：如模型、比例、导出模式（current/all）等
     */
    private String metaJson;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
