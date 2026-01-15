package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.PayEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付回调事件表 Mapper 接口
 */
@Mapper
public interface PayEventMapper extends BaseMapper<PayEvent> {
}
