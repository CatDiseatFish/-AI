package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.dto.wallet.TransactionQueryRequest;
import com.ym.ai_story_studio_server.dto.wallet.TransactionVO;
import com.ym.ai_story_studio_server.dto.wallet.WalletVO;
import com.ym.ai_story_studio_server.entity.Wallet;
import com.ym.ai_story_studio_server.entity.WalletTransaction;
import com.ym.ai_story_studio_server.mapper.WalletMapper;
import com.ym.ai_story_studio_server.mapper.WalletTransactionMapper;
import com.ym.ai_story_studio_server.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 钱包服务实现类
 *
 * <p>实现钱包余额查询和积分流水记录查询功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper transactionMapper;

    @Override
    public WalletVO getWallet(Long userId) {
        log.info("查询用户钱包: userId={}", userId);

        // 1. 查询钱包
        Wallet wallet = walletMapper.selectById(userId);

        // 2. 如果钱包不存在,自动创建
        if (wallet == null) {
            log.info("钱包不存在,自动创建: userId={}", userId);
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0);
            walletMapper.insert(wallet);
        }

        // 3. 转换为VO并返回
        return new WalletVO(
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getUpdatedAt()
        );
    }

    @Override
    public Page<TransactionVO> getTransactions(Long userId, TransactionQueryRequest request) {
        log.info("分页查询积分流水: userId={}, request={}", userId, request);

        // 1. 构建分页对象
        Page<WalletTransaction> page = new Page<>(request.page(), request.size());

        // 2. 构建查询条件
        LambdaQueryWrapper<WalletTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletTransaction::getUserId, userId);

        // 动态添加筛选条件
        if (request.type() != null && !request.type().isEmpty()) {
            queryWrapper.eq(WalletTransaction::getType, request.type());
        }
        if (request.bizType() != null && !request.bizType().isEmpty()) {
            queryWrapper.eq(WalletTransaction::getBizType, request.bizType());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(WalletTransaction::getCreatedAt);

        // 3. 执行分页查询
        page = transactionMapper.selectPage(page, queryWrapper);

        // 4. 转换为VO
        Page<TransactionVO> voPage = (Page<TransactionVO>) page.convert(transaction -> new TransactionVO(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getBizType(),
                transaction.getBizId(),
                transaction.getMetaJson(),
                transaction.getCreatedAt()
        ));

        log.info("查询成功: total={}, pages={}", voPage.getTotal(), voPage.getPages());
        return voPage;
    }
}
