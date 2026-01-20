package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.user.UpdateProfileRequest;
import com.ym.ai_story_studio_server.dto.user.UserProfileVO;

/**
 * 用户服务接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 获取用户信息（含积分余额）
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateProfile(Long userId, UpdateProfileRequest request);

}
