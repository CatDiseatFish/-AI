package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目表 Mapper 接口
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
