package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付订单（对接微信扫码支付，成功后发积分）
 */
@Data
@TableName("pay_orders")
public class PayOrder {

    /**
     * 支付订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 充值商品ID（可空：自定义金额）
     */
    private Long productId;

    /**
     * 兑换规则ID（自定义金额时使用）
     */
    private Long exchangeRuleId;

    /**
     * 商户订单号（业务侧生成，唯一）
     */
    private String orderNo;

    /**
     * 支付渠道：WECHAT等
     */
    private String provider;

    /**
     * 支付场景：NATIVE(网页扫码)/JSAPI/H5等
     */
    private String scene;

    /**
     * 状态：CREATED/PAYING/SUCCEEDED/FAILED/CANCELED/REFUNDED
     */
    private String status;

    /**
     * 订单金额（分）
     */
    private Integer amountCents;

    /**
     * 币种：默认CNY
     */
    private String currency;

    /**
     * 支付成功应到账积分（下单时计算并固化）
     */
    private Integer points;

    /**
     * 订单标题/商品描述（传给渠道）
     */
    private String title;

    /**
     * 下单客户端IP（可选）
     */
    private String clientIp;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 渠道预支付ID（如微信prepay_id）
     */
    private String providerPrepayId;

    /**
     * 扫码支付URL（如微信code_url）
     */
    private String providerPayUrl;

    /**
     * 渠道交易号（支付成功后回填）
     */
    private String providerTradeNo;

    /**
     * 支付完成时间
     */
    private LocalDateTime paidAt;

    /**
     * 关闭/取消时间
     */
    private LocalDateTime closedAt;

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
