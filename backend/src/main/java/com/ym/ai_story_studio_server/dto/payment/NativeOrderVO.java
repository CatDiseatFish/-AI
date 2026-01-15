package com.ym.ai_story_studio_server.dto.payment;

import java.time.LocalDateTime;

/**
 * Native支付订单响应VO
 *
 * <p>创建Native支付订单成功后的响应数据,包含二维码URL等关键信息
 *
 * <p>使用场景:
 * <ul>
 *   <li>前端接收codeUrl后生成二维码图片展示给用户</li>
 *   <li>前端使用orderNo轮询查询支付状态</li>
 *   <li>expireAt到期前用户必须完成支付,否则订单失效</li>
 * </ul>
 *
 * @param orderNo 商户订单号(唯一标识)
 * @param codeUrl 微信支付二维码链接(有效期2小时)
 * @param amountCents 订单金额(单位:分)
 * @param points 支付成功后将获得的积分数量
 * @param expireAt 订单过期时间(创建后2小时)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record NativeOrderVO(
        String orderNo,
        String codeUrl,
        Integer amountCents,
        Integer points,
        LocalDateTime expireAt
) {}
