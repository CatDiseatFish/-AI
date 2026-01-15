package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户钱包（积分余额）
 */
@Data
@TableName("wallets")
public class Wallet {

    /**
     * 用户ID（同 users.id，主键）
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 积分余额
     */
    private Integer balance;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
