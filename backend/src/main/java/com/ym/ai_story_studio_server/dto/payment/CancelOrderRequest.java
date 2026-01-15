package com.ym.ai_story_studio_server.dto.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * 取消支付订单请求
 *
 * <p>用于用户主动取消未支付的订单
 *
 * <p>取消条件:
 * <ul>
 *   <li>订单状态必须为CREATED(未支付)</li>
 *   <li>订单必须属于当前用户</li>
 *   <li>已支付的订单不能取消,只能申请退款</li>
 * </ul>
 *
 * @param orderNo 商户订单号
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CancelOrderRequest(
        @NotBlank(message = "订单号不能为空")
        String orderNo
) {}
