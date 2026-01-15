package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户认证表 Mapper 接口
 */
@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {
}
