package com.ym.ai_story_studio_server.dto.wallet;

import java.time.LocalDateTime;

/**
 * 钱包余额响应VO
 *
 * <p>返回用户钱包的当前积分余额信息
 *
 * @param userId 用户ID
 * @param balance 当前积分余额
 * @param updatedAt 最后更新时间
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record WalletVO(
        Long userId,
        Integer balance,
        LocalDateTime updatedAt
) {}
