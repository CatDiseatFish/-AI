package com.ym.ai_story_studio_server.dto.payment;

import java.time.LocalDateTime;

/**
 * 充值套餐响应VO
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record RechargeProductVO(
        Long id,
        String name,
        Integer points,
        Integer priceCents,
        Integer enabled,
        Integer sortOrder,
        String description,
        LocalDateTime createdAt
) {
}
