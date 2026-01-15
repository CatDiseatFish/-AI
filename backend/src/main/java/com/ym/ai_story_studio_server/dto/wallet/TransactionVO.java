package com.ym.ai_story_studio_server.dto.wallet;

import java.time.LocalDateTime;

/**
 * 积分流水记录响应VO
 *
 * <p>返回单条积分流水的详细信息
 *
 * @param id 流水ID
 * @param type 流水类型(RECHARGE=充值/CONSUME=消费/REFUND=退款/ADJUST=调整)
 * @param amount 变动积分(正数表示增加,负数表示减少)
 * @param balanceAfter 变动后的余额
 * @param bizType 业务类型(JOB=任务消费/ORDER=充值订单/INVITE=邀请奖励等)
 * @param bizId 业务ID(如job_id或order_id)
 * @param metaJson 扩展信息JSON字符串
 * @param createdAt 创建时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record TransactionVO(
        Long id,
        String type,
        Integer amount,
        Integer balanceAfter,
        String bizType,
        Long bizId,
        String metaJson,
        LocalDateTime createdAt
) {}
