package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包表 Mapper 接口
 */
@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
}
