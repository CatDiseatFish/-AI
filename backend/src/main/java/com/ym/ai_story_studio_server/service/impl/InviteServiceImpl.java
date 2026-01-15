package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.invite.InviteCodeVO;
import com.ym.ai_story_studio_server.dto.invite.InviteRecordVO;
import com.ym.ai_story_studio_server.dto.invite.InviteStatsVO;
import com.ym.ai_story_studio_server.entity.*;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.*;
import com.ym.ai_story_studio_server.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 邀请码服务实现类
 *
 * <p>实现邀请码的生成、使用、查询等功能,支持积分返利机制
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private final InviteCodeMapper inviteCodeMapper;
    private final InviteRecordMapper inviteRecordMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * 默认被邀请人获得的积分
     */
    private static final int DEFAULT_INVITEE_REWARD = 50;

    /**
     * 默认邀请人获得的积分
     */
    private static final int DEFAULT_INVITER_REWARD = 100;

    /**
     * 邀请码字符集(不包含容易混淆的字符,如0OIl)
     */
    private static final String INVITE_CODE_CHARS = "123456789ABCDEFGHJKMNPQRSTUVWXYZ";

    /**
     * 邀请码长度
     */
    private static final int INVITE_CODE_LENGTH = 8;

    @Override
    public InviteCodeVO getMyInviteCode(Long userId) {
        log.debug("获取用户的邀请码: userId={}", userId);

        // 查询用户的有效邀请码
        InviteCode inviteCode = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getUserId, userId)
                        .eq(InviteCode::getEnabled, 1)
                        .orderByDesc(InviteCode::getCreatedAt)
                        .last("LIMIT 1")
        );

        // 如果不存在,则自动生成
        if (inviteCode == null) {
            log.info("用户尚无邀请码,自动生成: userId={}", userId);
            return generateInviteCode(userId);
        }

        return convertToVO(inviteCode);
    }

    @Override
    @Transactional
    public InviteCodeVO generateInviteCode(Long userId) {
        log.info("生成新的邀请码: userId={}", userId);

        // 1. 禁用用户的所有旧邀请码
        List<InviteCode> oldCodes = inviteCodeMapper.selectList(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getUserId, userId)
                        .eq(InviteCode::getEnabled, 1)
        );

        for (InviteCode oldCode : oldCodes) {
            oldCode.setEnabled(0);
            inviteCodeMapper.updateById(oldCode);
            log.debug("禁用旧邀请码: code={}", oldCode.getCode());
        }

        // 2. 生成唯一的邀请码
        String code = generateUniqueCode();

        // 3. 创建新邀请码实体
        InviteCode inviteCode = new InviteCode();
        inviteCode.setUserId(userId);
        inviteCode.setCode(code);
        inviteCode.setRewardPoints(DEFAULT_INVITEE_REWARD);
        inviteCode.setInviterRewardPoints(DEFAULT_INVITER_REWARD);
        inviteCode.setMaxUses(null); // 不限制使用次数
        inviteCode.setUsedCount(0);
        inviteCode.setExpireAt(null); // 永不过期
        inviteCode.setEnabled(1);

        inviteCodeMapper.insert(inviteCode);

        log.info("邀请码生成成功: userId={}, code={}", userId, code);

        return convertToVO(inviteCode);
    }

    @Override
    @Transactional
    public void applyInviteCode(Long userId, String code) {
        log.info("使用邀请码: userId={}, code={}", userId, code);

        // 1. 查询邀请码是否存在
        InviteCode inviteCode = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getCode, code)
        );

        if (inviteCode == null) {
            log.warn("邀请码不存在: code={}", code);
            throw new BusinessException(ResultCode.INVITE_CODE_NOT_FOUND);
        }

        // 2. 验证邀请码有效性
        // 2.1 检查是否已禁用
        if (inviteCode.getEnabled() == 0) {
            log.warn("邀请码已禁用: code={}", code);
            throw new BusinessException(ResultCode.INVITE_CODE_DISABLED);
        }

        // 2.2 检查是否过期
        if (inviteCode.getExpireAt() != null && LocalDateTime.now().isAfter(inviteCode.getExpireAt())) {
            log.warn("邀请码已过期: code={}, expireAt={}", code, inviteCode.getExpireAt());
            throw new BusinessException(ResultCode.INVITE_CODE_EXPIRED);
        }

        // 2.3 检查使用次数是否已达上限
        if (inviteCode.getMaxUses() != null && inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            log.warn("邀请码使用次数已达上限: code={}, usedCount={}, maxUses={}",
                    code, inviteCode.getUsedCount(), inviteCode.getMaxUses());
            throw new BusinessException(ResultCode.INVITE_CODE_USED_UP);
        }

        // 3. 验证用户是否可以被邀请
        // 3.1 不能邀请自己
        if (inviteCode.getUserId().equals(userId)) {
            log.warn("不能使用自己的邀请码: userId={}", userId);
            throw new BusinessException(ResultCode.CANNOT_INVITE_SELF);
        }

        // 3.2 检查用户是否已被邀请过
        Long recordCount = inviteRecordMapper.selectCount(
                new LambdaQueryWrapper<InviteRecord>()
                        .eq(InviteRecord::getInviteeUserId, userId)
        );

        if (recordCount > 0) {
            log.warn("用户已被邀请过: userId={}", userId);
            throw new BusinessException(ResultCode.ALREADY_INVITED);
        }

        // 4. 发放积分
        Long inviterUserId = inviteCode.getUserId();
        Integer inviteeReward = inviteCode.getRewardPoints();
        Integer inviterReward = inviteCode.getInviterRewardPoints();

        // 4.1 给被邀请人发放积分
        addPoints(userId, inviteeReward, "INVITE", inviteCode.getId(), "使用邀请码获得奖励");

        // 4.2 给邀请人发放积分
        addPoints(inviterUserId, inviterReward, "INVITE", inviteCode.getId(), "邀请新用户获得奖励");

        // 5. 创建邀请记录
        InviteRecord record = new InviteRecord();
        record.setInviteCodeId(inviteCode.getId());
        record.setInviterUserId(inviterUserId);
        record.setInviteeUserId(userId);
        record.setInviterReward(inviterReward);
        record.setInviteeReward(inviteeReward);

        inviteRecordMapper.insert(record);

        // 6. 更新邀请码使用次数
        inviteCode.setUsedCount(inviteCode.getUsedCount() + 1);
        inviteCodeMapper.updateById(inviteCode);

        log.info("邀请码使用成功: userId={}, code={}, inviterReward={}, inviteeReward={}",
                userId, code, inviterReward, inviteeReward);
    }

    @Override
    public List<InviteRecordVO> getMyInviteRecords(Long userId) {
        log.debug("查询用户的邀请记录: userId={}", userId);

        // 查询用户作为邀请人的所有记录
        List<InviteRecord> records = inviteRecordMapper.selectList(
                new LambdaQueryWrapper<InviteRecord>()
                        .eq(InviteRecord::getInviterUserId, userId)
                        .orderByDesc(InviteRecord::getCreatedAt)
        );

        // 转换为VO
        return records.stream().map(record -> {
            // 查询被邀请人的昵称
            User inviteeUser = userMapper.selectById(record.getInviteeUserId());
            String inviteeNickname = (inviteeUser != null) ? inviteeUser.getNickname() : "未知用户";

            return new InviteRecordVO(
                    record.getId(),
                    record.getInviteeUserId(),
                    inviteeNickname,
                    record.getInviterReward(),
                    record.getInviteeReward(),
                    record.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public InviteStatsVO getInviteStats(Long userId) {
        log.debug("查询用户的邀请统计: userId={}", userId);

        // 统计总邀请人数
        Long totalInvited = inviteRecordMapper.selectCount(
                new LambdaQueryWrapper<InviteRecord>()
                        .eq(InviteRecord::getInviterUserId, userId)
        );

        // 统计总获得积分
        List<InviteRecord> records = inviteRecordMapper.selectList(
                new LambdaQueryWrapper<InviteRecord>()
                        .eq(InviteRecord::getInviterUserId, userId)
        );

        Integer totalRewards = records.stream()
                .mapToInt(InviteRecord::getInviterReward)
                .sum();

        return new InviteStatsVO(
                totalInvited.intValue(),
                totalRewards
        );
    }

    /**
     * 生成唯一的邀请码
     *
     * @return 唯一邀请码
     */
    private String generateUniqueCode() {
        Random random = new Random();
        String code;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        do {
            // 生成随机邀请码
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                int index = random.nextInt(INVITE_CODE_CHARS.length());
                sb.append(INVITE_CODE_CHARS.charAt(index));
            }
            code = sb.toString();

            // 检查是否已存在
            Long count = inviteCodeMapper.selectCount(
                    new LambdaQueryWrapper<InviteCode>()
                            .eq(InviteCode::getCode, code)
            );

            if (count == 0) {
                return code;
            }

            attempts++;
            log.debug("邀请码冲突,重新生成: code={}, attempts={}", code, attempts);

        } while (attempts < MAX_ATTEMPTS);

        // 如果10次都冲突,抛出异常
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "邀请码生成失败,请稍后重试");
    }

    /**
     * 给用户增加积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param remark 备注
     */
    private void addPoints(Long userId, Integer points, String bizType, Long bizId, String remark) {
        log.debug("增加用户积分: userId={}, points={}, bizType={}, bizId={}",
                userId, points, bizType, bizId);

        // 1. 查询或创建钱包
        Wallet wallet = walletMapper.selectById(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0);
            walletMapper.insert(wallet);
            log.info("用户钱包不存在,自动创建: userId={}", userId);
        }

        // 2. 更新余额
        Integer oldBalance = wallet.getBalance();
        Integer newBalance = oldBalance + points;
        wallet.setBalance(newBalance);
        walletMapper.updateById(wallet);

        // 3. 创建流水记录
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setType("RECHARGE"); // 邀请奖励属于充值类型
        transaction.setAmount(points);
        transaction.setBalanceAfter(newBalance);
        transaction.setBizType(bizType);
        transaction.setBizId(bizId);

        // 构建元数据JSON
        try {
            Map<String, String> meta = new HashMap<>();
            meta.put("remark", remark);
            transaction.setMetaJson(objectMapper.writeValueAsString(meta));
        } catch (Exception e) {
            log.warn("构建流水元数据JSON失败", e);
            transaction.setMetaJson(null);
        }

        walletTransactionMapper.insert(transaction);

        log.info("用户积分增加成功: userId={}, oldBalance={}, newBalance={}, points={}",
                userId, oldBalance, newBalance, points);
    }

    /**
     * 将实体转换为VO
     *
     * @param inviteCode 邀请码实体
     * @return 邀请码VO
     */
    private InviteCodeVO convertToVO(InviteCode inviteCode) {
        return new InviteCodeVO(
                inviteCode.getId(),
                inviteCode.getCode(),
                inviteCode.getRewardPoints(),
                inviteCode.getInviterRewardPoints(),
                inviteCode.getUsedCount(),
                inviteCode.getMaxUses(),
                inviteCode.getExpireAt(),
                inviteCode.getEnabled(),
                inviteCode.getCreatedAt()
        );
    }
}
