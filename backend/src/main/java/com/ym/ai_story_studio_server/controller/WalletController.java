package com.ym.ai_story_studio_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.wallet.TransactionQueryRequest;
import com.ym.ai_story_studio_server.dto.wallet.TransactionVO;
import com.ym.ai_story_studio_server.dto.wallet.WalletVO;
import com.ym.ai_story_studio_server.service.WalletService;
import com.ym.ai_story_studio_server.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包控制器
 *
 * <p>提供钱包余额查询和积分流水记录查询接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * 获取钱包余额
     *
     * <p>GET /api/wallet
     *
     * <p>返回当前登录用户的钱包信息,包括积分余额和最后更新时间
     * <p>如果钱包不存在,系统会自动创建初始余额为0的钱包
     *
     * @return 统一响应结果,包含钱包信息
     */
    @GetMapping
    public Result<WalletVO> getWallet() {
        Long userId = UserContext.getUserId();
        log.info("获取钱包余额: userId={}", userId);

        WalletVO wallet = walletService.getWallet(userId);
        return Result.success(wallet);
    }

    /**
     * 分页查询积分流水记录
     *
     * <p>GET /api/wallet/transactions
     *
     * <p>支持按流水类型、业务类型筛选,按创建时间倒序排列
     *
     * @param request 查询请求参数
     *                - type: 流水类型筛选(RECHARGE/CONSUME/REFUND/ADJUST),可选
     *                - bizType: 业务类型筛选(JOB/ORDER/INVITE等),可选
     *                - page: 页码,默认1
     *                - size: 每页大小,默认20,最大100
     * @return 统一响应结果,包含分页的流水记录列表
     */
    @GetMapping("/transactions")
    public Result<Page<TransactionVO>> getTransactions(TransactionQueryRequest request) {
        Long userId = UserContext.getUserId();
        log.info("查询积分流水: userId={}, request={}", userId, request);

        Page<TransactionVO> page = walletService.getTransactions(userId, request);
        return Result.success(page);
    }
}
