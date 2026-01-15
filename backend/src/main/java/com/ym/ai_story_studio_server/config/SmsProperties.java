package com.ym.ai_story_studio_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信服务配置属性类
 *
 * <p>从application.yml中绑定sms配置项
 *
 * <p>配置示例：
 * <pre>
 * sms:
 *   provider: mock  # mock / aliyun
 *   aliyun:
 *     access-key-id: xxx
 *     access-key-secret: xxx
 *     sign-name: 圆梦动画
 *     template-code: SMS_123456
 * </pre>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /**
     * 短信服务提供商
     * <p>可选值: mock (开发环境), aliyun (生产环境)
     */
    private String provider = "mock";

    /**
     * 阿里云SMS配置
     */
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 阿里云SMS配置项
     */
    @Data
    public static class AliyunConfig {
        /**
         * 阿里云AccessKey ID
         */
        private String accessKeyId;

        /**
         * 阿里云AccessKey Secret
         */
        private String accessKeySecret;

        /**
         * 短信签名
         */
        private String signName;

        /**
         * 短信模板CODE
         */
        private String templateCode;
    }
}
