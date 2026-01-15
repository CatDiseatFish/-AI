package com.ym.ai_story_studio_server.service.impl;

import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.user.UpdateProfileRequest;
import com.ym.ai_story_studio_server.dto.user.UserProfileVO;
import com.ym.ai_story_studio_server.entity.User;
import com.ym.ai_story_studio_server.entity.Wallet;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.UserMapper;
import com.ym.ai_story_studio_server.mapper.WalletMapper;
import com.ym.ai_story_studio_server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 查询钱包信息（积分余额）
        Wallet wallet = walletMapper.selectById(userId);
        Integer balance = (wallet != null) ? wallet.getBalance() : 0;

        // 3. 组装VO
        log.info("获取用户信息成功: userId={}, nickname={}", userId, user.getNickname());
        return new UserProfileVO(
                user.getId(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getStatus(),
                balance,
                user.getCreatedAt()
        );
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 更新用户信息
        boolean needUpdate = false;

        if (request.nickname() != null) {
            user.setNickname(request.nickname());
            needUpdate = true;
            log.info("更新昵称: userId={}, newNickname={}", userId, request.nickname());
        }

        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
            needUpdate = true;
            log.info("更新头像: userId={}, newAvatarUrl={}", userId, request.avatarUrl());
        }

        // 3. 保存到数据库
        if (needUpdate) {
            userMapper.updateById(user);
            log.info("用户信息更新成功: userId={}", userId);
        } else {
            log.info("无需更新用户信息: userId={}", userId);
        }
    }
}
