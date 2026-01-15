package com.ym.ai_story_studio_server.dto.wallet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 积分流水查询请求
 *
 * <p>用于分页查询用户的积分流水记录
 *
 * @param type 流水类型筛选(RECHARGE/CONSUME/REFUND/ADJUST),可选
 * @param bizType 业务类型筛选(JOB/ORDER/INVITE等),可选
 * @param page 页码,默认1
 * @param size 每页大小,默认20,最大100
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record TransactionQueryRequest(
        String type,
        String bizType,

        @Min(value = 1, message = "页码必须大于0")
        Integer page,

        @Min(value = 1, message = "每页大小必须大于0")
        @Max(value = 100, message = "每页大小不能超过100")
        Integer size
) {
    public TransactionQueryRequest {
        page = page == null ? 1 : page;
        size = size == null ? 20 : size;
    }
}
