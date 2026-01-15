package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.ShotBinding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分镜绑定关系表 Mapper 接口
 */
@Mapper
public interface ShotBindingMapper extends BaseMapper<ShotBinding> {
}
