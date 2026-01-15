package com.ym.ai_story_studio_server.service;

/**
 * 短信服务接口
 *
 * <p>采用策略模式，支持多种短信服务提供商
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface SmsService {

    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code 验证码
     * @return true=发送成功，false=发送失败
     */
    boolean sendVerificationCode(String phone, String code);
}
