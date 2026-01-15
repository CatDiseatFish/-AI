package com.ym.ai_story_studio_server.util;

import com.ym.ai_story_studio_server.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 *
 * <p>负责JWT Token的生成、解析和验证
 *
 * <p>核心功能：
 * <ul>
 *   <li>生成JWT Token（包含userId和过期时间）</li>
 *   <li>从Token中解析userId</li>
 *   <li>验证Token有效性（签名、过期时间）</li>
 * </ul>
 *
 * <p>技术实现：
 * <ul>
 *   <li>使用jjwt 0.12.5库</li>
 *   <li>签名算法：HS256</li>
 *   <li>密钥缓存：SecretKey对象复用，提升性能</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 生成Token
 * String token = jwtUtil.generateToken(userId);
 *
 * // 验证Token
 * if (jwtUtil.validateToken(token)) {
 *     Long userId = jwtUtil.getUserIdFromToken(token);
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * 缓存的密钥对象
     * <p>SecretKey创建开销较大，缓存复用提升性能
     */
    private SecretKey secretKey;

    /**
     * 获取密钥对象（懒加载 + 缓存）
     *
     * @return SecretKey对象
     */
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            // 将配置的密钥字符串转换为SecretKey对象
            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return secretKey;
    }

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @return JWT Token字符串
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))           // 设置主题为userId
                .issuedAt(now)                             // 签发时间
                .expiration(expiryDate)                    // 过期时间
                .signWith(getSecretKey())                  // 使用密钥签名（默认HS256算法）
                .compact();
    }

    /**
     * 从Token中解析userId
     *
     * @param token JWT Token字符串
     * @return 用户ID
     * @throws ExpiredJwtException Token已过期
     * @throws SignatureException 签名验证失败
     * @throws MalformedJwtException Token格式错误
     * @throws IllegalArgumentException Token为null或空字符串
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())                // 设置验证密钥
                .build()
                .parseSignedClaims(token)                  // 解析并验证签名
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    /**
     * 验证Token有效性
     *
     * @param token JWT Token字符串
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空");
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return false;

        } catch (SignatureException e) {
            log.warn("Token签名验证失败: {}", e.getMessage());
            return false;

        } catch (MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Token验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 判断Token是否已过期
     *
     * @param token JWT Token字符串
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date());

        } catch (ExpiredJwtException e) {
            // Token已过期
            return true;
        } catch (Exception e) {
            // 其他异常也视为过期
            log.error("判断Token过期时发生异常: {}", e.getMessage());
            return true;
        }
    }
}
