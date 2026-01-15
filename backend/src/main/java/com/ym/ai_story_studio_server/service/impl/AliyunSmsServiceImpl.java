package com.ym.ai_story_studio_server.service.impl;

import com.ym.ai_story_studio_server.config.SmsProperties;
import com.ym.ai_story_studio_server.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信服务实现（生产环境）
 *
 * <p>当配置 sms.provider=aliyun 时生效
 * <p>TODO: 集成阿里云SMS SDK
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "aliyun")
@RequiredArgsConstructor
public class AliyunSmsServiceImpl implements SmsService {

    private final SmsProperties smsProperties;

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        // TODO: V2版本集成阿里云SMS SDK
        // 实现步骤：
        // 1. 添加阿里云SMS SDK依赖到pom.xml
        //    <dependency>
        //        <groupId>com.aliyun</groupId>
        //        <artifactId>dysmsapi20170525</artifactId>
        //        <version>2.0.24</version>
        //    </dependency>
        //
        // 2. 创建DefaultAcsClient
        //    DefaultProfile profile = DefaultProfile.getProfile(
        //        "cn-hangzhou",
        //        smsProperties.getAliyun().getAccessKeyId(),
        //        smsProperties.getAliyun().getAccessKeySecret()
        //    );
        //    IAcsClient client = new DefaultAcsClient(profile);
        //
        // 3. 构造SendSmsRequest
        //    SendSmsRequest request = new SendSmsRequest();
        //    request.setPhoneNumbers(phone);
        //    request.setSignName(smsProperties.getAliyun().getSignName());
        //    request.setTemplateCode(smsProperties.getAliyun().getTemplateCode());
        //    request.setTemplateParam("{\"code\":\"" + code + "\"}");
        //
        // 4. 发送短信并处理响应
        //    SendSmsResponse response = client.getAcsResponse(request);
        //    if ("OK".equals(response.getCode())) {
        //        log.info("【阿里云SMS】短信发送成功: phone={}", maskPhone(phone));
        //        return true;
        //    } else {
        //        log.error("【阿里云SMS】短信发送失败: code={}, message={}",
        //            response.getCode(), response.getMessage());
        //        return false;
        //    }

        log.warn("【阿里云SMS】服务尚未实现，请在生产环境前完成集成");
        log.warn("【配置信息】signName={}, templateCode={}",
                smsProperties.getAliyun().getSignName(),
                smsProperties.getAliyun().getTemplateCode());

        // 临时返回false，生产环境需实现真实逻辑
        return false;
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
