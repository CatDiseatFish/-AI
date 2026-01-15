package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.Asset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产表 Mapper 接口
 */
@Mapper
public interface AssetMapper extends BaseMapper<Asset> {
}
