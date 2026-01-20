package com.ym.ai_story_studio_server.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * <p>封装Redis操作，用于验证码存储和防刷控制
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    /**
     * 验证码Key前缀
     */
    private static final String CODE_PREFIX = "SMS:CODE:";

    /**
     * 防刷锁Key前缀
     */
    private static final String LOCK_PREFIX = "SMS:LOCK:";

    /**
     * 图形验证码Key前缀
     */
    private static final String CAPTCHA_PREFIX = "CAPTCHA:";

    /**
     * 存储验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @param seconds 有效期（秒）
     */
    public void saveCode(String phone, String code, long seconds) {
        String key = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, seconds, TimeUnit.SECONDS);
        log.debug("存储验证码到Redis: key={}, TTL={}秒", key, seconds);
    }

    /**
     * 获取验证码
     *
     * @param phone 手机号
     * @return 验证码，如果不存在或已过期返回null
     */
    public String getCode(String phone) {
        String key = CODE_PREFIX + phone;
        String code = redisTemplate.opsForValue().get(key);
        log.debug("从Redis获取验证码: key={}, code={}", key, code != null ? "******" : "null");
        return code;
    }

    /**
     * 删除验证码
     *
     * @param phone 手机号
     */
    public void deleteCode(String phone) {
        String key = CODE_PREFIX + phone;
        redisTemplate.delete(key);
        log.debug("从Redis删除验证码: key={}", key);
    }

    /**
     * 尝试设置防刷锁
     *
     * @param phone 手机号
     * @param seconds 锁定时长（秒）
     * @return true=设置成功，false=锁已存在
     */
    public boolean tryLock(String phone, long seconds) {
        String key = LOCK_PREFIX + phone;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", seconds, TimeUnit.SECONDS);
        log.debug("尝试设置防刷锁: key={}, 结果={}", key, success);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 检查防刷锁是否存在
     *
     * @param phone 手机号
     * @return true=锁存在，false=锁不存在
     */
    public boolean hasLock(String phone) {
        String key = LOCK_PREFIX + phone;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 存储图形验证码
     *
     * @param captchaId 验证码ID
     * @param code 验证码内容
     * @param seconds 有效期（秒）
     */
    public void saveCaptcha(String captchaId, String code, long seconds) {
        String key = CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, code, seconds, TimeUnit.SECONDS);
        log.debug("存储图形验证码到Redis: key={}, TTL={}秒", key, seconds);
    }

    /**
     * 获取图形验证码
     *
     * @param captchaId 验证码ID
     * @return 验证码内容
     */
    public String getCaptcha(String captchaId) {
        String key = CAPTCHA_PREFIX + captchaId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除图形验证码
     *
     * @param captchaId 验证码ID
     */
    public void deleteCaptcha(String captchaId) {
        String key = CAPTCHA_PREFIX + captchaId;
        redisTemplate.delete(key);
    }
}
