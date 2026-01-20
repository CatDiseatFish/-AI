package com.ym.ai_story_studio_server.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.ym.ai_story_studio_server.config.SmsProperties;
import com.ym.ai_story_studio_server.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
@ConditionalOnExpression("'${sms.provider:mock}' == 'aliyun' || '${sms.provider:mock}' == 'true'")
@RequiredArgsConstructor
public class AliyunSmsServiceImpl implements SmsService {

    private final SmsProperties smsProperties;

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        if (!hasValidConfig()) {
            log.error("【阿里云SMS】配置不完整，请检查 accessKeyId/accessKeySecret/signName/templateCode");
            return false;
        }

        try {
            Client client = createClient();
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsProperties.getAliyun().getSignName())
                    .setTemplateCode(smsProperties.getAliyun().getTemplateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = client.sendSms(request);
            String responseCode = response.getBody() != null ? response.getBody().getCode() : null;
            if ("OK".equalsIgnoreCase(responseCode)) {
                log.info("【阿里云SMS】短信发送成功: phone={}", maskPhone(phone));
                return true;
            }

            String message = response.getBody() != null ? response.getBody().getMessage() : "unknown";
            log.error("【阿里云SMS】短信发送失败: code={}, message={}", responseCode, message);
            return false;
        } catch (Exception e) {
            log.error("【阿里云SMS】发送异常: phone={}, error={}", maskPhone(phone), e.getMessage(), e);
            return false;
        }
    }

    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(smsProperties.getAliyun().getAccessKeyId())
                .setAccessKeySecret(smsProperties.getAliyun().getAccessKeySecret());
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }

    private boolean hasValidConfig() {
        SmsProperties.AliyunConfig aliyun = smsProperties.getAliyun();
        return aliyun.getAccessKeyId() != null && !aliyun.getAccessKeyId().isBlank()
                && aliyun.getAccessKeySecret() != null && !aliyun.getAccessKeySecret().isBlank()
                && aliyun.getSignName() != null && !aliyun.getSignName().isBlank()
                && aliyun.getTemplateCode() != null && !aliyun.getTemplateCode().isBlank();
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
