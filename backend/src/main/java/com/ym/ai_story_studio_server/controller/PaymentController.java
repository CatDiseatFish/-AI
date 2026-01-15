package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.annotation.NoAuth;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.payment.CancelOrderRequest;
import com.ym.ai_story_studio_server.dto.payment.CreateNativeOrderRequest;
import com.ym.ai_story_studio_server.dto.payment.ExchangeRuleVO;
import com.ym.ai_story_studio_server.dto.payment.NativeOrderVO;
import com.ym.ai_story_studio_server.dto.payment.OrderStatusVO;
import com.ym.ai_story_studio_server.dto.payment.RechargeProductVO;
import com.ym.ai_story_studio_server.service.PaymentService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付控制器
 *
 * <p>提供微信Native扫码支付相关的HTTP接口:
 * <ul>
 *   <li>POST /api/recharge/orders - 创建支付订单(需要JWT)</li>
 *   <li>GET /api/recharge/orders/{orderNo} - 查询订单状态(需要JWT)</li>
 *   <li>POST /api/recharge/notify/wechat - 微信支付回调(无需JWT)</li>
 *   <li>POST /api/recharge/orders/{orderNo}/cancel - 取消订单(需要JWT)</li>
 * </ul>
 *
 * <p>注意事项:
 * <ul>
 *   <li>微信回调接口不能使用JWT认证,需要在SecurityInterceptor中配置白名单</li>
 *   <li>微信回调接口必须返回成功响应,否则微信会重复发送通知</li>
 *   <li>所有金额单位为"分",前端需要做除以100的转换</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/recharge")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建Native扫码支付订单
     *
     * <p>前端流程:
     * <ol>
     *   <li>用户选择充值套餐或输入自定义金额</li>
     *   <li>调用此接口创建订单</li>
     *   <li>接收codeUrl并生成二维码图片</li>
     *   <li>用户使用微信扫码支付</li>
     *   <li>前端轮询查询订单状态接口,直到支付成功</li>
     * </ol>
     *
     * @param request 创建订单请求(productId或amountCents二选一)
     * @return Native订单响应VO(包含codeUrl)
     */
    @PostMapping("/orders")
    public Result<NativeOrderVO> createOrder(@Valid @RequestBody CreateNativeOrderRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建支付订单请求: userId={}, request={}", userId, request);

        NativeOrderVO orderVO = paymentService.createNativeOrder(userId, request);

        return Result.success("订单创建成功", orderVO);
    }

    /**
     * 查询订单支付状态
     *
     * <p>用于前端轮询查询支付结果
     *
     * <p>轮询建议:
     * <ul>
     *   <li>前30秒: 每2秒查询一次</li>
     *   <li>30秒-2分钟: 每5秒查询一次</li>
     *   <li>2分钟后: 提示用户支付超时</li>
     * </ul>
     *
     * @param orderNo 商户订单号
     * @return 订单状态VO
     */
    @GetMapping("/orders/{orderNo}")
    public Result<OrderStatusVO> queryOrderStatus(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        log.info("查询订单状态: userId={}, orderNo={}", userId, orderNo);

        OrderStatusVO statusVO = paymentService.queryOrderStatus(userId, orderNo);

        return Result.success(statusVO);
    }

    /**
     * 微信支付回调通知(无需JWT认证)
     *
     * <p>微信支付成功后会异步调用此接口通知支付结果
     *
     * <p>重要说明:
     * <ul>
     *   <li>使用@NoAuth注解绕过JWT拦截器验证</li>
     *   <li>必须返回HTTP 200和成功响应,否则微信会重复发送通知(最多10次)</li>
     *   <li>回调处理已实现幂等性,重复通知不会重复发放积分</li>
     * </ul>
     *
     * <p>实际使用中,此接口需要接收微信加密的通知数据,
     * 使用微信SDK进行解密和验签,然后提取Transaction对象。
     *
     * @param request HTTP请求对象，用于获取微信回调的原始数据
     * @return 成功响应
     */
    @NoAuth
    @PostMapping("/notify/wechat")
    public Result<Void> wechatCallback(HttpServletRequest request) {
        log.info("接收微信支付回调: 接收到回调请求");

        try {
            // 实际的微信支付回调处理应该：
            // 1. 从request中获取微信的加密回调数据
            // 2. 使用微信SDK进行验签和解密
            // 3. 转换为Transaction对象
            // 4. 调用服务层处理方法
            
            // 这里需要使用微信支付SDK来处理回调数据
            // 由于微信支付SDK需要处理加密和验签，实现会比较复杂
            // 下面是示意代码，实际应用中需要完整的实现
            paymentService.handleWechatCallback(request);
            
            log.info("微信支付回调处理完成");
            
            // 返回成功响应给微信
            return Result.success();

        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            // 即使处理失败,也要返回成功响应,避免微信重复推送
            // 失败的回调可以通过日志和人工介入处理
            return Result.success();
        }
    }

    /**
     * 取消未支付的订单
     *
     * <p>用户可以主动取消未支付的订单
     *
     * <p>取消条件:
     * <ul>
     *   <li>订单状态必须为CREATED(未支付)</li>
     *   <li>已支付的订单不能取消,只能申请退款</li>
     * </ul>
     *
     * @param orderNo 商户订单号
     * @param request 取消订单请求(目前只需要orderNo)
     * @return 成功响应
     */
    @PostMapping("/orders/{orderNo}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable String orderNo,
            @Valid @RequestBody CancelOrderRequest request) {
        Long userId = UserContext.getUserId();
        log.info("取消订单请求: userId={}, orderNo={}", userId, orderNo);

        paymentService.cancelOrder(userId, orderNo);

        return Result.success();
    }

    /**
     * 获取充值套餐列表
     *
     * <p>返回所有启用的充值套餐，按排序值排序
     *
     * @return 充值套餐列表
     */
    @GetMapping("/products")
    public Result<List<RechargeProductVO>> getProducts() {
        log.info("获取充值套餐列表");
        List<RechargeProductVO> products = paymentService.getRechargeProducts();
        return Result.success(products);
    }

    /**
     * 获取兑换规则列表
     *
     * <p>返回所有启用的兑换规则
     *
     * @return 兑换规则列表
     */
    @GetMapping("/exchange-rules")
    public Result<List<ExchangeRuleVO>> getExchangeRules() {
        log.info("获取兑换规则列表");
        List<ExchangeRuleVO> rules = paymentService.getExchangeRules();
        return Result.success(rules);
    }
}
