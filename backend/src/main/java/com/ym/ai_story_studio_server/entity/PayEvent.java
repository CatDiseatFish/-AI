package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付回调事件记录（幂等、审计、排障）
 */
@Data
@TableName("pay_events")
public class PayEvent {

    /**
     * 支付事件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付渠道：WECHAT等
     */
    private String provider;

    /**
     * 渠道事件ID/通知ID（用于幂等）
     */
    private String eventId;

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 渠道交易号
     */
    private String providerTradeNo;

    /**
     * 事件类型：PAY_SUCCESS/PAY_FAIL/REFUND等
     */
    private String eventType;

    /**
     * 回调原始内容（JSON）
     */
    private String payloadJson;

    /**
     * 是否已处理：1是，0否
     */
    private Integer processed;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
