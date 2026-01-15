package com.ym.ai_story_studio_server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.dto.wallet.TransactionQueryRequest;
import com.ym.ai_story_studio_server.dto.wallet.TransactionVO;
import com.ym.ai_story_studio_server.dto.wallet.WalletVO;

/**
 * 钱包服务接口
 *
 * <p>提供钱包余额查询、积分流水记录查询等功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface WalletService {

    /**
     * 获取用户钱包信息
     *
     * <p>如果钱包不存在,自动创建初始余额为0的钱包
     *
     * @param userId 用户ID
     * @return 钱包信息VO
     */
    WalletVO getWallet(Long userId);

    /**
     * 分页查询用户的积分流水记录
     *
     * <p>支持按流水类型、业务类型筛选,按创建时间倒序排列
     *
     * @param userId 用户ID
     * @param request 查询请求参数(包含分页参数和筛选条件)
     * @return 分页的流水记录列表
     */
    Page<TransactionVO> getTransactions(Long userId, TransactionQueryRequest request);
}
