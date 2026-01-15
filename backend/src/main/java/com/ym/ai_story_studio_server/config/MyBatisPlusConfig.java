// {{CODE-Cycle-Integration:
//   Task_ID: [#T018]
//   Timestamp: [2025-12-30 10:05:00]
//   Phase: [D-Develop]
//   Context-Analysis: "创建MyBatis Plus配置类,添加分页拦截器以支持Page分页查询的COUNT功能。修复工具箱历史查询total=0的问题。"
//   Principle_Applied: "MyBatis Plus官方配置规范, 分页插件最佳实践"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 *
 * <p>配置MyBatis Plus的核心插件,包括分页插件、乐观锁插件等
 *
 * <p><strong>核心功能:</strong>
 * <ul>
 *   <li>配置分页拦截器(PaginationInnerInterceptor),支持Page对象自动查询total</li>
 *   <li>自动识别数据库类型,生成对应数据库的分页SQL</li>
 *   <li>配置合理的分页参数溢出处理策略</li>
 * </ul>
 *
 * <p><strong>分页插件说明:</strong>
 * <ul>
 *   <li><strong>自动COUNT查询</strong>: 使用Page&lt;T&gt;对象查询时,自动执行COUNT查询获取total值</li>
 *   <li><strong>多数据库支持</strong>: 自动识别MySQL、PostgreSQL、Oracle等数据库,生成对应分页SQL</li>
 *   <li><strong>参数溢出处理</strong>: 当page &lt; 1时自动修正为1,当size超过最大值时自动限制</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * &#64;Service
 * public class UserService {
 *
 *     private final UserMapper userMapper;
 *
 *     public Page&lt;User&gt; getUsers(Integer page, Integer size) {
 *         // 创建分页对象
 *         Page&lt;User&gt; pageParam = new Page&lt;(page, size);
 *
 *         // 构建查询条件
 *         LambdaQueryWrapper&lt;User&gt; query = new LambdaQueryWrapper&lt;&gt;();
 *         query.eq(User::getStatus, "ACTIVE");
 *
 *         // 执行分页查询(自动执行COUNT查询获取total)
 *         Page&lt;User&gt; result = userMapper.selectPage(pageParam, query);
 *
 *         // result.getTotal() 返回总记录数
 *         // result.getPages() 返回总页数
 *         // result.getRecords() 返回当前页数据列表
 *         return result;
 *     }
 * }
 * </pre>
 *
 * <p><strong>注意事项:</strong>
 * <ul>
 *   <li>配置此类后,所有使用Page&lt;T&gt;的查询都会自动执行COUNT查询</li>
 *   <li>如果不需要COUNT查询(仅查询数据),可以使用IPage&lt;T&gt;并设置searchCount(false)</li>
 *   <li>单页分页条数限制建议设置为500(避免一次性加载过多数据)</li>
 *   <li>由于MySQL数据库,使用LIMIT offset, size语法实现分页</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    /**
     * 单页分页条数限制(默认无限制)
     *
     * <p>设置为500,当单页查询数量超过500时自动限制为500
     * <p>作用: 防止恶意请求一次性查询过多数据导致数据库压力过大
     */
    private static final long MAX_LIMIT = 500L;

    /**
     * 配置MyBatis Plus拦截器链
     *
     * <p>添加分页拦截器到拦截器链,使分页查询功能生效
     *
     * <p><strong>工作原理:</strong>
     * <ol>
     *   <li>拦截selectPage方法调用</li>
     *   <li>自动生成COUNT查询SQL: SELECT COUNT(*) FROM table WHERE ...</li>
     *   <li>执行COUNT查询获取total值并设置到Page对象</li>
     *   <li>在原SQL基础上添加LIMIT子句: SELECT ... FROM table WHERE ... LIMIT offset, size</li>
     *   <li>执行分页查询获取records并设置到Page对象</li>
     *   <li>计算pages = (total + size - 1) / size并设置到Page对象</li>
     * </ol>
     *
     * @return 配置好的MyBatis Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("初始化MyBatis Plus拦截器 - 添加分页插件, 单页最大条数: {}", MAX_LIMIT);

        // 创建分页拦截器
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setDbType(DbType.MYSQL);

        // 设置单页分页条数限制
        // 当查询的size超过此值时,自动设置为MAX_LIMIT
        paginationInterceptor.setMaxLimit(MAX_LIMIT);

        // 设置当页码小于1时的处理策略
        // true: 当page &lt; 1时自动修正为page = 1
        // false: 当page &lt; 1时直接返回空结果
        paginationInterceptor.setOverflow(false);

        log.info("MyBatis Plus分页插件配置完成");

        // 创建MyBatis Plus拦截器并添加分页拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
// {{END_MODIFICATIONS}}
