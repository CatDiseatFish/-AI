package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务表 Mapper 接口
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
