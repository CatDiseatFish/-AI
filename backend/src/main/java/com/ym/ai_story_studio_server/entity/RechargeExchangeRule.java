package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义充值金额兑换规则
 */
@Data
@TableName("recharge_exchange_rules")
public class RechargeExchangeRule {

    /**
     * 兑换规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 币种：默认CNY
     */
    private String currency;

    /**
     * 每多少"分"兑换1积分（如10表示0.1元=1积分）
     */
    private Integer centsPerPoint;

    /**
     * 最小充值金额（分）
     */
    private Integer minAmountCents;

    /**
     * 最大充值金额（分，可空表示不限制）
     */
    private Integer maxAmountCents;

    /**
     * 是否启用：1启用，0禁用
     */
    private Integer enabled;

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
