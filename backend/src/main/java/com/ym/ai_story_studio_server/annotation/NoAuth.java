package com.ym.ai_story_studio_server.annotation;

import java.lang.annotation.*;

/**
 * 免认证注解
 *
 * <p>用于标记不需要JWT认证的接口（如登录、注册、健康检查等公开API）
 *
 * <p>使用示例：
 * <pre>
 * // 方法级别
 * &#64;NoAuth
 * &#64;PostMapping("/api/auth/phone/login")
 * public Result&lt;LoginVO&gt; login(&#64;RequestBody LoginDTO dto) {
 *     // 登录逻辑
 * }
 *
 * // 类级别（整个Controller的所有方法都免认证）
 * &#64;NoAuth
 * &#64;RestController
 * &#64;RequestMapping("/api/public")
 * public class PublicController {
 *     // ...
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}
