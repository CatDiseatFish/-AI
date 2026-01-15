package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包流水表 Mapper 接口
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
}
