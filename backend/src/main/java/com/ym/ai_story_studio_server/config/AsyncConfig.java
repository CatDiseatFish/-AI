// {{CODE-Cycle-Integration:
//   Task_ID: [#T017]
//   Timestamp: [2025-12-28 20:00:00]
//   Phase: [D-Develop]
//   Context-Analysis: "创建异步执行配置类,配置自定义线程池用于异步任务执行。基于Spring Boot 3.5.9标准,确保线程池参数合理且支持异步任务追踪。"
//   Principle_Applied: "SOLID原则-单一职责, 企业级线程池配置最佳实践, 异常处理机制"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行配置类
 *
 * <p>配置Spring异步任务执行器,用于处理所有AI生成任务(文本解析、角色生成、场景生成、分镜生成、视频生成、导出ZIP等)
 *
 * <p><strong>核心功能:</strong>
 * <ul>
 *   <li>配置自定义线程池执行器(ThreadPoolTaskExecutor)</li>
 *   <li>设置合理的线程池参数(核心线程数、最大线程数、队列容量等)</li>
 *   <li>配置异步任务异常处理器</li>
 *   <li>支持异步任务追踪和日志记录</li>
 * </ul>
 *
 * <p><strong>线程池参数说明:</strong>
 * <ul>
 *   <li><strong>核心线程数(CorePoolSize)</strong>: 10个,始终存活的线程数</li>
 *   <li><strong>最大线程数(MaxPoolSize)</strong>: 50个,线程池最大可创建的线程数</li>
 *   <li><strong>队列容量(QueueCapacity)</strong>: 200个任务,超过核心线程数的任务会进入队列等待</li>
 *   <li><strong>空闲线程存活时间(KeepAliveSeconds)</strong>: 60秒,超过核心线程数的空闲线程将在60秒后被回收</li>
 * </ul>
 *
 * <p><strong>拒绝策略:</strong>
 * 当队列满且线程数达到最大时,使用CallerRunsPolicy策略:
 * <ul>
 *   <li>任务不会被丢弃</li>
 *   <li>由调用线程自己执行该任务(降级执行)</li>
 *   <li>适合任务不能丢失的场景</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * &#64;Service
 * public class TaskService {
 *
 *     &#64;Async("taskExecutor")
 *     public CompletableFuture<String> executeAsyncTask() {
 *         // 异步执行的任务逻辑
 *         return CompletableFuture.completedFuture("任务完成");
 *     }
 * }
 * </pre>
 *
 * <p><strong>注意事项:</strong>
 * <ul>
 *   <li>异步方法必须是public修饰符</li>
 *   <li>异步方法不能在同一个类内部调用(需要通过Spring代理调用)</li>
 *   <li>异步方法的返回值类型应该是void或CompletableFuture&lt;T&gt;</li>
 *   <li>异步方法中抛出的异常会被AsyncUncaughtExceptionHandler捕获</li>
 *   <li>异步方法中不能直接使用ThreadLocal(包括UserContext),需要手动传递参数</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "Async-Task-";

    /**
     * 核心线程数(始终存活的线程数)
     *
     * <p>设置为10个,适合处理中等并发的AI生成任务
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 最大线程数(线程池最大可创建的线程数)
     *
     * <p>设置为50个,在高峰期可以创建更多线程处理突发流量
     */
    private static final int MAX_POOL_SIZE = 50;

    /**
     * 队列容量(超过核心线程数的任务会进入队列等待)
     *
     * <p>设置为200个,可以缓冲大量等待执行的任务
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * 空闲线程存活时间(秒)
     *
     * <p>超过核心线程数的空闲线程将在60秒后被回收
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 配置异步任务执行器(线程池)
     *
     * <p>创建一个自定义的ThreadPoolTaskExecutor,用于执行所有标记了@Async的异步方法
     *
     * <p><strong>线程池工作流程:</strong>
     * <ol>
     *   <li>任务提交后,如果当前线程数 < 核心线程数,创建新线程执行任务</li>
     *   <li>如果当前线程数 >= 核心线程数,任务进入队列等待</li>
     *   <li>如果队列已满且当前线程数 < 最大线程数,创建新线程执行任务</li>
     *   <li>如果队列已满且当前线程数 >= 最大线程数,执行拒绝策略(CallerRunsPolicy)</li>
     * </ol>
     *
     * @return 配置好的线程池执行器
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        log.info("初始化异步任务执行器 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);

        // 创建线程池执行器
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);

        // 设置最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);

        // 设置队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);

        // 设置空闲线程存活时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);

        // 设置线程名称前缀(方便日志追踪)
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        // 设置拒绝策略: CallerRunsPolicy
        // 当队列满且线程数达到最大时,由调用线程自己执行该任务(降级执行)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 设置是否等待任务完成后再关闭线程池
        // true: 在应用关闭时等待所有任务完成后再关闭线程池
        // false: 立即关闭线程池,不等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 设置等待任务完成的超时时间(秒)
        // 应用关闭时最多等待60秒,超时后强制关闭线程池
        executor.setAwaitTerminationSeconds(60);

        // 初始化线程池
        executor.initialize();

        log.info("异步任务执行器初始化完成");

        return executor;
    }

    /**
     * 配置异步任务异常处理器
     *
     * <p>当异步方法抛出未捕获的异常时,会调用此处理器记录错误日志
     *
     * <p><strong>注意:</strong>
     * <ul>
     *   <li>如果异步方法返回CompletableFuture,异常会通过CompletableFuture.exceptionally()处理</li>
     *   <li>如果异步方法返回void,异常会通过此处理器捕获并记录日志</li>
     * </ul>
     *
     * @return 异步任务异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * 自定义异步任务异常处理器
     *
     * <p>负责处理异步方法中抛出的未捕获异常,记录详细的错误日志
     */
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        /**
         * 处理异步任务异常
         *
         * @param throwable 异常对象
         * @param method 抛出异常的方法
         * @param params 方法参数
         */
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
            log.error("============================异步任务执行失败============================");
            log.error("异常方法: {}.{}", method.getDeclaringClass().getName(), method.getName());
            log.error("方法参数: {}", params);
            log.error("异常信息: {}", throwable.getMessage(), throwable);
            log.error("======================================================================");
        }
    }
}
// {{END_MODIFICATIONS}}
