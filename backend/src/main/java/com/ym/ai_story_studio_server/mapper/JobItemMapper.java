package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.JobItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务项表 Mapper 接口
 */
@Mapper
public interface JobItemMapper extends BaseMapper<JobItem> {
}
