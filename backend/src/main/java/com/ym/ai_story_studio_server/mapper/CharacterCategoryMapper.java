package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.CharacterCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色分类表 Mapper 接口
 */
@Mapper
public interface CharacterCategoryMapper extends BaseMapper<CharacterCategory> {
}
