package com.ym.ai_story_studio_server.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ym.ai_story_studio_server.annotation.NoAuth;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.config.JwtProperties;
import com.ym.ai_story_studio_server.util.JwtUtil;
import com.ym.ai_story_studio_server.util.UserContext;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 *
 * <p>拦截所有HTTP请求，验证JWT Token的有效性
 *
 * <p>核心职责：
 * <ul>
 *   <li>检查接口是否标记@NoAuth注解，如果有则放行</li>
 *   <li>从请求头提取JWT Token</li>
 *   <li>验证Token有效性（签名、过期时间）</li>
 *   <li>解析userId并存入Request Attribute和UserContext</li>
 *   <li>认证失败时返回统一格式的错误响应</li>
 * </ul>
 *
 * <p>拦截流程：
 * <pre>
 * 1. 检查@NoAuth注解 → 有则放行
 * 2. 提取Token → 无则返回401 UNAUTHORIZED
 * 3. 验证Token → 无效则返回对应错误码
 * 4. 解析userId → 存入上下文
 * 5. 放行请求
 * </pre>
 *
 * <p>错误码映射：
 * <ul>
 *   <li>Token缺失 → 40100 UNAUTHORIZED</li>
 *   <li>Token格式错误 → 40101 TOKEN_INVALID</li>
 *   <li>Token已过期 → 40102 TOKEN_EXPIRED</li>
 *   <li>签名验证失败 → 40101 TOKEN_INVALID</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    /**
     * 请求处理前拦截
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true=放行，false=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 检查是否为HandlerMethod（Controller方法）
        if (!(handler instanceof HandlerMethod)) {
            // 不是Controller方法（如静态资源），直接放行
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 2. 检查方法或类是否标记@NoAuth注解
        boolean hasNoAuth = handlerMethod.hasMethodAnnotation(NoAuth.class)
                || handlerMethod.getBeanType().isAnnotationPresent(NoAuth.class);

        if (hasNoAuth) {
            // 标记了@NoAuth，直接放行
            log.debug("接口 {} 标记@NoAuth，无需认证", request.getRequestURI());
            return true;
        }

        // 3. 从请求头获取Token
        String authHeader = request.getHeader(jwtProperties.getHeader());

        if (authHeader == null || authHeader.trim().isEmpty()) {
            // 兼容下载等场景：浏览器<a>触发的GET请求无法携带Authorization头，
            // 允许通过 query 参数 token 传递（仅作为兜底）。
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.trim().isEmpty()) {
                authHeader = jwtProperties.getPrefix() + tokenParam.trim();
            }
        }

        if (authHeader == null || authHeader.trim().isEmpty()) {
            // Token缺失
            log.warn("请求 {} 未携带Token", request.getRequestURI());
            return handleAuthError(response, ResultCode.UNAUTHORIZED, "未登录或登录已过期，请先登录");
        }

        // 4. 检查Token前缀
        String prefix = jwtProperties.getPrefix();
        if (!authHeader.startsWith(prefix)) {
            // Token格式错误
            log.warn("请求 {} 的Token格式错误，缺少前缀: {}", request.getRequestURI(), prefix);
            return handleAuthError(response, ResultCode.TOKEN_INVALID, "Token格式错误");
        }

        // 5. 去除前缀，获取纯Token
        String token = authHeader.substring(prefix.length());

        try {
            // 6. 验证Token有效性
            if (!jwtUtil.validateToken(token)) {
                log.warn("请求 {} 的Token验证失败", request.getRequestURI());
                return handleAuthError(response, ResultCode.TOKEN_INVALID, "Token无效");
            }

            // 7. 解析userId
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 8. 将userId存入Request Attribute（供Controller使用）
            request.setAttribute("userId", userId);

        // 9. 将userId存入UserContext（供Service层使用）
        UserContext.setUserId(userId);

        // 10. 读取API密钥(仅本次请求有效)
        String apiKey = request.getHeader("X-Api-Key");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            UserContext.setApiKey(apiKey.trim());
        }

            log.debug("用户 {} 通过JWT认证，访问接口: {}", userId, request.getRequestURI());
            return true;

        } catch (ExpiredJwtException e) {
            // Token已过期
            log.warn("请求 {} 的Token已过期: {}", request.getRequestURI(), e.getMessage());
            return handleAuthError(response, ResultCode.TOKEN_EXPIRED, "Token已过期，请重新登录");

        } catch (SignatureException e) {
            // 签名验证失败
            log.warn("请求 {} 的Token签名验证失败: {}", request.getRequestURI(), e.getMessage());
            return handleAuthError(response, ResultCode.TOKEN_INVALID, "Token签名验证失败");

        } catch (Exception e) {
            // 其他异常
            log.error("请求 {} 的Token解析异常: {}", request.getRequestURI(), e.getMessage(), e);
            return handleAuthError(response, ResultCode.UNAUTHORIZED, "认证失败，请重新登录");
        }
    }

    /**
     * 请求处理完成后清理
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常（如果有）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理UserContext，防止内存泄漏
        UserContext.clear();
        log.debug("清理UserContext完成");
    }

    /**
     * 处理认证错误，返回统一格式的JSON响应
     *
     * @param response   HTTP响应
     * @param resultCode 错误码
     * @param message    错误消息
     * @return false（拦截请求）
     * @throws Exception IO异常
     */
    private boolean handleAuthError(HttpServletResponse response, ResultCode resultCode, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.error(resultCode, message);
        String json = objectMapper.writeValueAsString(result);

        response.getWriter().write(json);
        return false;
    }
}
