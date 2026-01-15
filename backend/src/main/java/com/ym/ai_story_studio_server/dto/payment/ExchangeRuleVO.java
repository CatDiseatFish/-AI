package com.ym.ai_story_studio_server.dto.payment;

/**
 * 兑换规则响应VO
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ExchangeRuleVO(
        Long id,
        String name,
        Integer pointsPerYuan,  // 每1元兑换多少积分（前端展示用）
        Integer enabled
) {
    /**
     * 从实体转换为VO
     *
     * @param centsPerPoint 每多少分兑换1积分（数据库存储）
     * @return 每1元兑换多少积分（前端展示）
     */
    public static Integer calculatePointsPerYuan(Integer centsPerPoint) {
        if (centsPerPoint == null || centsPerPoint == 0) {
            return 0;
        }
        // 100分（1元） / centsPerPoint = pointsPerYuan
        return 100 / centsPerPoint;
    }
}
