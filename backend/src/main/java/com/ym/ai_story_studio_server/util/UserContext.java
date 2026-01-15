package com.ym.ai_story_studio_server.util;

/**
 * 用户上下文工具类
 *
 * <p>使用ThreadLocal存储当前请求的用户ID，实现线程安全的用户信息传递
 *
 * <p>使用场景：
 * <ul>
 *   <li>在JWT拦截器中设置当前登录用户ID</li>
 *   <li>在Service层获取当前用户ID，实现数据隔离</li>
 *   <li>在请求结束时清理ThreadLocal，防止内存泄漏</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 在拦截器中设置userId
 * UserContext.setUserId(userId);
 *
 * // 在Service中获取userId
 * Long currentUserId = UserContext.getUserId();
 *
 * // 在请求结束时清理（拦截器的afterCompletion方法中）
 * UserContext.clear();
 * </pre>
 *
 * <p><strong>注意事项</strong>：
 * <ul>
 *   <li>必须在请求结束时调用clear()方法清理ThreadLocal</li>
 *   <li>建议在拦截器的afterCompletion方法中进行清理</li>
 *   <li>不清理可能导致内存泄漏和线程复用时的数据串台</li>
 * </ul>
 */
public class UserContext {

    /**
     * ThreadLocal存储当前请求的用户ID
     */
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    /**
     * 设置当前请求的用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    /**
     * 获取当前请求的用户ID
     *
     * @return 用户ID，如果未设置则返回null
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }

    /**
     * 清除当前请求的用户ID
     *
     * <p>必须在请求结束时调用，防止内存泄漏和线程复用时的数据串台
     */
    public static void clear() {
        userIdHolder.remove();
    }
}
