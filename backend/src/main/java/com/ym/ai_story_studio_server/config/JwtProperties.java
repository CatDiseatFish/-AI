package com.ym.ai_story_studio_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 *
 * <p>从application.yml中绑定jwt配置项
 *
 * <p>配置示例：
 * <pre>
 * jwt:
 *   secret: your-secret-key
 *   expiration: 604800000
 *   header: Authorization
 *   prefix: "Bearer "
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     * <p>生产环境建议使用环境变量配置，长度至少256位
     */
    private String secret;

    /**
     * Token过期时间（毫秒）
     * <p>默认7天 = 7 * 24 * 60 * 60 * 1000 = 604800000
     */
    private Long expiration;

    /**
     * Token请求头名称
     * <p>默认: Authorization
     */
    private String header = "Authorization";

    /**
     * Token前缀
     * <p>默认: "Bearer "（注意有空格）
     */
    private String prefix = "Bearer ";
}
