package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水明细（可审计/可追溯）
 */
@Data
@TableName("wallet_transactions")
public class WalletTransaction {

    /**
     * 流水ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 流水类型：RECHARGE/CONSUME/REFUND/ADJUST
     */
    private String type;

    /**
     * 变动积分：推荐消费为负数，充值为正数
     */
    private Integer amount;

    /**
     * 变动后的余额
     */
    private Integer balanceAfter;

    /**
     * 业务类型：JOB/ORDER等
     */
    private String bizType;

    /**
     * 业务ID：如 job_id 或 order_id
     */
    private Long bizId;

    /**
     * 扩展信息（JSON）：如模型、数量、备注等
     */
    private String metaJson;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
