package com.ym.ai_story_studio_server.service.impl;

import com.ym.ai_story_studio_server.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * Mock短信服务实现（开发环境）
 *
 * <p>仅在控制台输出验证码，不实际发送短信
 * <p>当配置 sms.provider=mock 时生效
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnExpression("'${sms.provider:mock}' == 'mock' || '${sms.provider:mock}' == 'false'")
public class MockSmsServiceImpl implements SmsService {

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        // 脱敏显示手机号
        String maskedPhone = phone.substring(0, 3) + "****" + phone.substring(7);

        log.info("=================================================");
        log.info("【Mock短信服务】向手机号 {} 发送验证码", maskedPhone);
        log.info("【验证码】: {}", code);
        log.info("【有效期】: 5分钟");
        log.info("【提示】: 这是开发环境的Mock服务，验证码仅在控制台输出");
        log.info("=================================================");

        return true;
    }
}
