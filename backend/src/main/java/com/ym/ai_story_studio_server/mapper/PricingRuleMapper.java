package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.PricingRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计费规则表 Mapper 接口
 */
@Mapper
public interface PricingRuleMapper extends BaseMapper<PricingRule> {
}
