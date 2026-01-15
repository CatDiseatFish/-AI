package com.ym.ai_story_studio_server.dto.payment;

import java.time.LocalDateTime;

/**
 * 订单状态响应VO
 *
 * <p>查询支付订单状态的响应数据
 *
 * <p>订单状态说明:
 * <ul>
 *   <li>CREATED: 订单已创建,等待支付</li>
 *   <li>PAYING: 用户正在支付(可选状态)</li>
 *   <li>SUCCEEDED: 支付成功,积分已发放</li>
 *   <li>FAILED: 支付失败</li>
 *   <li>CANCELED: 订单已取消</li>
 *   <li>REFUNDED: 已退款</li>
 * </ul>
 *
 * @param orderNo 商户订单号
 * @param status 订单状态(CREATED/SUCCEEDED/FAILED/CANCELED/REFUNDED)
 * @param amountCents 订单金额(单位:分)
 * @param points 应得积分数量
 * @param paidAt 支付完成时间(仅当status=SUCCEEDED时有值)
 * @param createdAt 订单创建时间
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record OrderStatusVO(
        String orderNo,
        String status,
        Integer amountCents,
        Integer points,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {}
