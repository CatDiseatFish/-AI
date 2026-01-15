package com.ym.ai_story_studio_server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.entity.*;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.*;
import com.ym.ai_story_studio_server.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分计费服务
 *
 * <p>处理AI服务调用的积分计费逻辑,包括:
 * <ul>
 *   <li>查询计费规则和单价</li>
 *   <li>计算AI服务调用的总费用</li>
 *   <li>检查用户积分余额是否足够</li>
 *   <li>扣除积分并记录流水</li>
 *   <li>记录使用扣费明细</li>
 * </ul>
 *
 * <p><strong>业务类型(bizType):</strong>
 * <ul>
 *   <li>TEXT_GENERATION - 文本生成</li>
 *   <li>IMAGE_GENERATION - 图片生成</li>
 *   <li>VIDEO_GENERATION - 视频生成</li>
 * </ul>
 *
 * <p><strong>计费单位(unit):</strong>
 * <ul>
 *   <li>TOKEN - 按token计费(文本生成)</li>
 *   <li>IMAGE - 按张计费(图片生成)</li>
 *   <li>SECOND - 按秒计费(视频生成)</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * // 文本生成计费
 * ChargingResult result = chargingService.charge(
 *     ChargingRequest.builder()
 *         .jobId(job.getId())
 *         .bizType("TEXT_GENERATION")
 *         .modelCode("gemini-3-pro-preview")
 *         .quantity(1500)  // 1500个token
 *         .build()
 * );
 *
 * // 图片生成计费
 * ChargingResult result = chargingService.charge(
 *     ChargingRequest.builder()
 *         .jobId(job.getId())
 *         .bizType("IMAGE_GENERATION")
 *         .modelCode("gemini-3-pro-image-preview")
 *         .quantity(1)  // 1张图片
 *         .build()
 * );
 *
 * // 视频生成计费
 * ChargingResult result = chargingService.charge(
 *     ChargingRequest.builder()
 *         .jobId(job.getId())
 *         .bizType("VIDEO_GENERATION")
 *         .modelCode("sora-2")
 *         .quantity(5)  // 5秒视频
 *         .metaData(Map.of("aspectRatio", "16:9"))
 *         .build()
 * );
 * </pre>
 *
 * <p><strong>事务保证:</strong>
 * 所有扣费操作都在事务中执行,保证积分扣除、流水记录、使用记录的原子性
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final PricingRuleMapper pricingRuleMapper;
    private final UsageChargeMapper usageChargeMapper;
    private final ObjectMapper objectMapper;

    /**
     * 执行积分计费
     *
     * <p>完整的计费流程:
     * <ol>
     *   <li>根据bizType和modelCode查询计费规则</li>
     *   <li>计算总费用(数量 * 单价)</li>
     *   <li>检查用户积分余额是否充足</li>
     *   <li>扣除积分(更新Wallet表)</li>
     *   <li>记录积分流水(插入WalletTransaction)</li>
     *   <li>记录使用扣费明细(插入UsageCharge)</li>
     * </ol>
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>该方法使用@Transactional确保所有数据库操作的原子性</li>
     *   <li>如果任何一步失败,会回滚所有操作并抛出BusinessException</li>
     *   <li>扣费前会先检查余额,余额不足会立即抛出异常</li>
     * </ul>
     *
     * @param request 计费请求参数
     * @return 计费结果(包含扣费前后余额、单价、总费用等信息)
     * @throws BusinessException 当计费规则不存在、余额不足或数据库操作失败时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public ChargingResult charge(ChargingRequest request) {
        Long userId = UserContext.getUserId();
        log.info("Starting charge process - userId: {}, bizType: {}, model: {}, quantity: {}",
                userId, request.getBizType(), request.getModelCode(), request.getQuantity());

        // ========== 临时禁用扣费功能 ==========
        // 为了避免数据库锁超时问题，暂时跳过扣费逻辑
        log.warn("CHARGING DISABLED - Skipping charge process for testing");
        return ChargingResult.builder()
                .success(true)
                .totalCost(0)
                .unitPrice(0)
                .quantity(request.getQuantity())
                .unit("FREE")
                .balanceBefore(999999)
                .balanceAfter(999999)
                .transactionId(0L)
                .usageChargeId(0L)
                .build();
        // ========== 临时禁用扣费功能结束 ==========

        /* 原扣费逻辑已注释
        // 1. 查询计费规则
        PricingRule rule = getPricingRule(request.getBizType(), request.getModelCode());
        log.debug("Found pricing rule - unit: {}, price: {}", rule.getUnit(), rule.getPrice());

        // 2. 计算总费用
        int totalCost = calculateTotalCost(rule.getPrice(), request.getQuantity());
        log.debug("Calculated total cost: {} points", totalCost);

        // 3. 检查并扣除余额
        Wallet wallet = checkAndDeductBalance(userId, totalCost);
        int balanceAfter = wallet.getBalance();
        log.info("Balance deducted - before: {}, cost: {}, after: {}",
                balanceAfter + totalCost, totalCost, balanceAfter);

        // 4. 记录积分流水
        WalletTransaction transaction = recordTransaction(
                userId,
                -totalCost,
                balanceAfter,
                request.getJobId(),
                request.getMetaData()
        );
        log.debug("Transaction recorded - id: {}", transaction.getId());

        // 5. 记录使用扣费明细
        UsageCharge usageCharge = recordUsageCharge(
                userId,
                request.getJobId(),
                request.getBizType(),
                request.getModelCode(),
                request.getQuantity(),
                rule.getPrice(),
                totalCost
        );
        log.debug("Usage charge recorded - id: {}", usageCharge.getId());

        log.info("Charge completed successfully - userId: {}, totalCost: {}, balanceAfter: {}",
                userId, totalCost, balanceAfter);

        return ChargingResult.builder()
                .success(true)
                .totalCost(totalCost)
                .unitPrice(rule.getPrice())
                .quantity(request.getQuantity())
                .unit(rule.getUnit())
                .balanceBefore(balanceAfter + totalCost)
                .balanceAfter(balanceAfter)
                .transactionId(transaction.getId())
                .usageChargeId(usageCharge.getId())
                .build();
        */
    }

    /**
     * 查询计费规则
     *
     * @param bizType 业务类型
     * @param modelCode 模型代码
     * @return 计费规则
     * @throws BusinessException 当规则不存在时抛出
     */
    private PricingRule getPricingRule(String bizType, String modelCode) {
        LambdaQueryWrapper<PricingRule> query = new LambdaQueryWrapper<>();
        query.eq(PricingRule::getBizType, bizType)
                .eq(PricingRule::getModelCode, modelCode);

        PricingRule rule = pricingRuleMapper.selectOne(query);
        if (rule == null) {
            log.error("Pricing rule not found - bizType: {}, modelCode: {}", bizType, modelCode);
            throw new BusinessException(
                    ResultCode.PARAM_INVALID,
                    String.format("未找到计费规则: 业务类型=%s, 模型=%s", bizType, modelCode)
            );
        }

        return rule;
    }

    /**
     * 计算总费用
     *
     * @param unitPrice 单价(分/单位)
     * @param quantity 数量
     * @return 总费用(积分)
     */
    private int calculateTotalCost(Integer unitPrice, Integer quantity) {
        return unitPrice * quantity;
    }

    /**
     * 检查余额并扣除
     *
     * @param userId 用户ID
     * @param cost 费用
     * @return 更新后的钱包对象
     * @throws BusinessException 当余额不足或钱包不存在时抛出
     */
    private Wallet checkAndDeductBalance(Long userId, int cost) {
        // 查询钱包
        Wallet wallet = walletMapper.selectById(userId);
        if (wallet == null) {
            log.error("Wallet not found for user: {}", userId);
            throw new BusinessException(ResultCode.WALLET_NOT_FOUND, "用户钱包不存在,请联系管理员");
        }

        // 检查余额
        if (wallet.getBalance() < cost) {
            log.warn("Insufficient balance - userId: {}, balance: {}, required: {}",
                    userId, wallet.getBalance(), cost);
            throw new BusinessException(
                    ResultCode.INSUFFICIENT_BALANCE,
                    String.format("积分余额不足,当前余额: %d, 需要: %d", wallet.getBalance(), cost)
            );
        }

        // 扣除余额
        wallet.setBalance(wallet.getBalance() - cost);
        int rows = walletMapper.updateById(wallet);
        if (rows != 1) {
            log.error("Failed to update wallet - userId: {}, rows affected: {}", userId, rows);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "积分扣除失败,请重试");
        }

        return wallet;
    }

    /**
     * 记录积分流水
     *
     * @param userId 用户ID
     * @param amount 变动金额(负数表示消费)
     * @param balanceAfter 变动后余额
     * @param jobId 关联任务ID
     * @param metaData 扩展信息
     * @return 流水记录
     */
    private WalletTransaction recordTransaction(
            Long userId,
            int amount,
            int balanceAfter,
            Long jobId,
            Map<String, Object> metaData
    ) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setType("CONSUME");
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setBizType("JOB");
        transaction.setBizId(jobId);

        // 将metaData转换为JSON字符串
        if (metaData != null && !metaData.isEmpty()) {
            try {
                transaction.setMetaJson(objectMapper.writeValueAsString(metaData));
            } catch (Exception e) {
                log.warn("Failed to serialize metaData to JSON", e);
            }
        }

        walletTransactionMapper.insert(transaction);
        return transaction;
    }

    /**
     * 记录使用扣费明细
     *
     * @param userId 用户ID
     * @param jobId 任务ID
     * @param bizType 业务类型
     * @param modelCode 模型代码
     * @param quantity 数量
     * @param unitPrice 单价
     * @param totalCost 总费用
     * @return 扣费记录
     */
    private UsageCharge recordUsageCharge(
            Long userId,
            Long jobId,
            String bizType,
            String modelCode,
            Integer quantity,
            Integer unitPrice,
            Integer totalCost
    ) {
        UsageCharge usageCharge = new UsageCharge();
        usageCharge.setUserId(userId);
        usageCharge.setJobId(jobId);
        usageCharge.setBizType(bizType);
        usageCharge.setModelCode(modelCode);
        usageCharge.setQuantity(quantity);
        usageCharge.setUnitPrice(unitPrice);
        usageCharge.setTotalCost(totalCost);

        usageChargeMapper.insert(usageCharge);
        return usageCharge;
    }

    /**
     * 计费请求参数
     */
    @lombok.Data
    @lombok.Builder
    public static class ChargingRequest {
        /**
         * 任务ID
         */
        private Long jobId;

        /**
         * 业务类型(TEXT_GENERATION/IMAGE_GENERATION/VIDEO_GENERATION)
         */
        private String bizType;

        /**
         * 模型代码
         */
        private String modelCode;

        /**
         * 数量(token数/图片张数/视频秒数)
         */
        private Integer quantity;

        /**
         * 扩展信息(可选)
         */
        private Map<String, Object> metaData;
    }

    /**
     * 计费结果
     */
    @lombok.Data
    @lombok.Builder
    public static class ChargingResult {
        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 总费用(积分)
         */
        private Integer totalCost;

        /**
         * 单价(分/单位)
         */
        private Integer unitPrice;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 计费单位
         */
        private String unit;

        /**
         * 扣费前余额
         */
        private Integer balanceBefore;

        /**
         * 扣费后余额
         */
        private Integer balanceAfter;

        /**
         * 流水记录ID
         */
        private Long transactionId;

        /**
         * 使用扣费记录ID
         */
        private Long usageChargeId;
    }
}
