package com.ym.ai_story_studio_server.config;

import com.ym.ai_story_studio_server.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * <p>配置Spring MVC相关设置，包括拦截器注册
 *
 * <p>核心功能：
 * <ul>
 *   <li>注册JWT拦截器到拦截器链</li>
 *   <li>配置拦截和排除路径</li>
 *   <li>设置拦截器执行顺序</li>
 * </ul>
 *
 * <p>拦截规则：
 * <ul>
 *   <li><strong>拦截所有路径</strong>：/**</li>
 *   <li><strong>排除路径</strong>：
 *     <ul>
 *       <li>/api/health - 健康检查接口</li>
 *       <li>/api/auth/** - 认证相关接口（登录、注册、发送验证码等）</li>
 *       <li>/error - Spring Boot错误页面</li>
 *       <li>/favicon.ico - 浏览器图标</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>注意</strong>：
 * 排除路径配置只是一层额外保护，实际的免认证控制主要通过@NoAuth注解实现。
 * 如果某个接口既不在排除路径中，也没有@NoAuth注解，则必须携带有效Token才能访问。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                // 排除以下路径（这些路径无需JWT认证）
                .excludePathPatterns(
                        "/api/health",              // 健康检查
                        "/api/auth/**",             // 认证相关接口（登录、注册、发送验证码等）
                        "/error",                   // Spring Boot错误页面
                        "/favicon.ico",             // 浏览器图标
                        "/actuator/**"              // Spring Boot Actuator端点（如果启用）
                )
                // 拦截器执行顺序（数字越小越先执行）
                .order(1);
    }
}
