package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 计费规则（不同模型不同价格）
 */
@Data
@TableName("pricing_rules")
public class PricingRule {

    /**
     * 计费规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型：IMAGE/VIDEO/TEXT
     */
    private String bizType;

    /**
     * 模型标识：jimeng-4.5/sora-2/gemini等
     */
    private String modelCode;

    /**
     * 计费单位：PER_ITEM/PER_SECOND/PER_1K_TOKENS等
     */
    private String unit;

    /**
     * 单价（积分）
     */
    private Integer price;

    /**
     * 是否启用：1启用，0禁用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
