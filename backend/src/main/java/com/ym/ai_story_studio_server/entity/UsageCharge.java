package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 使用扣费记录（便于审计与对账）
 */
@Data
@TableName("usage_charges")
public class UsageCharge {

    /**
     * 扣费记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联任务ID
     */
    private Long jobId;

    /**
     * 业务类型：IMAGE/VIDEO/TEXT
     */
    private String bizType;

    /**
     * 模型标识
     */
    private String modelCode;

    /**
     * 计费数量（如张数/秒数/千tokens单位数）
     */
    private Integer quantity;

    /**
     * 单价（积分）
     */
    private Integer unitPrice;

    /**
     * 总费用（积分，通常为正数；实际扣减写入钱包流水）
     */
    private Integer totalCost;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
