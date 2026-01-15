package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单表 Mapper 接口
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
