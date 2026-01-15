package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.CharacterLibrary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色库表 Mapper 接口
 */
@Mapper
public interface CharacterLibraryMapper extends BaseMapper<CharacterLibrary> {
}
