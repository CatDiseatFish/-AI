package com.ym.ai_story_studio_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 充值商品（套餐）
 */
@Data
@TableName("recharge_products")
public class RechargeProduct {

    /**
     * 充值商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 到账积分
     */
    private Integer points;

    /**
     * 售价（分）
     */
    private Integer priceCents;

    /**
     * 是否启用：1启用，0禁用
     */
    private Integer enabled;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
