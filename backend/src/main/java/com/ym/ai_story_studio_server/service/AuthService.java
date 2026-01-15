package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.auth.PhoneLoginVO;

/**
 * 认证服务接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 发送验证码
     *
     * @param phone 手机号
     */
    void sendVerificationCode(String phone);

    /**
     * 手机号登录
     * <p>首次登录自动注册用户并创建钱包
     *
     * @param phone 手机号
     * @param code 验证码
     * @param inviteCode 邀请码（可选）
     * @return 登录信息（含Token）
     */
    PhoneLoginVO phoneLogin(String phone, String code, String inviteCode);

    /**
     * 微信登录（V2规划）
     *
     * @param code 微信授权码
     * @return 登录信息（含Token）
     */
    PhoneLoginVO wechatLogin(String code);
}
