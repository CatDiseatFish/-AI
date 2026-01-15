package com.ym.ai_story_studio_server.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 创建Native支付订单请求
 *
 * <p>用于创建微信Native扫码支付订单,支持两种方式:
 * <ul>
 *   <li>指定充值商品ID: 使用预设套餐的金额和积分</li>
 *   <li>自定义金额: 根据兑换规则计算应得积分</li>
 * </ul>
 *
 * <p>注意: productId和amountCents必须提供其中之一,不能同时为空
 *
 * @param productId 充值商品ID(可选,与amountCents二选一)
 * @param amountCents 自定义金额-分(可选,与productId二选一)
 * @param clientIp 客户端IP地址(可选,用于风控)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record CreateNativeOrderRequest(
        Long productId,

        @Min(value = 1, message = "金额必须大于0")
        Integer amountCents,

        String clientIp
) {
    /**
     * 验证请求参数的有效性
     *
     * @throws IllegalArgumentException 如果参数无效
     */
    public void validate() {
        if (productId == null && amountCents == null) {
            throw new IllegalArgumentException("productId和amountCents必须提供其中之一");
        }
        if (productId != null && amountCents != null) {
            throw new IllegalArgumentException("productId和amountCents不能同时提供");
        }
    }
}
