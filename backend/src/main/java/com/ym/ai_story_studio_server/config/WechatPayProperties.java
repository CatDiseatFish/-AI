package com.ym.ai_story_studio_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性类
 *
 * <p>绑定application.yml中的wechat.pay配置项,用于初始化微信支付SDK
 *
 * <p>配置示例:
 * <pre>
 * wechat:
 *   pay:
 *     merchant-id: 1234567890
 *     app-id: wx1234567890abcdef
 *     api-v3-key: your-api-v3-key-32-characters
 *     private-key-path: classpath:wechat/apiclient_key.pem
 *     merchant-serial-number: 1234567890ABCDEF
 *     notify-url: https://yourdomain.com/api/recharge/notify/wechat
 * </pre>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

    /**
     * 商户号
     *
     * <p>微信支付分配的商户号,10位数字
     */
    private String merchantId;

    /**
     * 应用ID(公众号ID/小程序ID)
     *
     * <p>微信公众号或小程序的AppID,用于标识应用身份
     */
    private String appId;

    /**
     * API V3密钥
     *
     * <p>用于解密微信支付回调通知的密钥,32字符长度
     * <p>在微信支付商户平台设置,需妥善保管,不可泄露
     */
    private String apiV3Key;

    /**
     * 商户API私钥文件路径
     *
     * <p>商户私钥PEM文件路径,用于API请求签名
     * <p>支持classpath:路径和绝对路径
     * <p>示例: classpath:wechat/apiclient_key.pem
     */
    private String privateKeyPath;

    /**
     * 商户证书序列号
     *
     * <p>商户API证书的序列号,用于请求签名
     * <p>可在微信支付商户平台或证书文件中查看
     */
    private String merchantSerialNumber;

    /**
     * 支付回调通知URL
     *
     * <p>微信支付成功后的异步通知地址,必须为HTTPS协议
     * <p>示例: https://yourdomain.com/api/recharge/notify/wechat
     * <p>注意: 此URL不能带JWT认证,微信服务器无法携带token
     */
    private String notifyUrl;
}
