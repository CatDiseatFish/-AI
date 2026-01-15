package com.ym.ai_story_studio_server.service;

import com.wechat.pay.java.service.payments.model.Transaction;
import com.ym.ai_story_studio_server.dto.payment.CreateNativeOrderRequest;
import com.ym.ai_story_studio_server.dto.payment.ExchangeRuleVO;
import com.ym.ai_story_studio_server.dto.payment.NativeOrderVO;
import com.ym.ai_story_studio_server.dto.payment.OrderStatusVO;
import com.ym.ai_story_studio_server.dto.payment.RechargeProductVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 支付服务接口
 *
 * <p>提供微信Native扫码支付的完整业务流程:
 * <ul>
 *   <li>创建支付订单并生成二维码</li>
 *   <li>查询订单支付状态</li>
 *   <li>处理微信支付回调通知(核心)</li>
 *   <li>取消未支付订单</li>
 * </ul>
 *
 * <p>安全机制:
 * <ul>
 *   <li>签名验证: 使用微信SDK验证回调签名</li>
 *   <li>幂等性保证: 通过pay_events表的唯一索引防止重复处理</li>
 *   <li>事务一致性: 回调处理使用@Transactional保证数据一致性</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface PaymentService {

    /**
     * 创建Native扫码支付订单
     *
     * <p>业务流程:
     * <ol>
     *   <li>生成唯一订单号(UUID)</li>
     *   <li>查询充值商品或计算自定义金额的积分</li>
     *   <li>创建PayOrder记录(status=CREATED)</li>
     *   <li>调用微信Native下单API获取code_url</li>
     *   <li>更新PayOrder的provider_prepay_id和provider_pay_url</li>
     *   <li>返回二维码URL和订单信息</li>
     * </ol>
     *
     * @param userId 用户ID(从JWT获取)
     * @param request 创建订单请求(包含productId或amountCents)
     * @return Native订单响应VO(包含codeUrl用于生成二维码)
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果商品不存在或创建订单失败
     */
    NativeOrderVO createNativeOrder(Long userId, CreateNativeOrderRequest request);

    /**
     * 查询支付订单状态
     *
     * <p>用于前端轮询查询支付结果
     *
     * <p>业务逻辑:
     * <ol>
     *   <li>根据orderNo查询PayOrder</li>
     *   <li>验证订单是否属于当前用户</li>
     *   <li>返回订单状态信息</li>
     * </ol>
     *
     * @param userId 用户ID(从JWT获取)
     * @param orderNo 商户订单号
     * @return 订单状态VO
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果订单不存在或无权限访问
     */
    OrderStatusVO queryOrderStatus(Long userId, String orderNo);

    /**
     * 处理微信支付回调通知(核心方法)
     *
     * <p>这是支付流程中最关键的方法,负责:
     * <ol>
     *   <li>验证微信签名(SDK自动完成)</li>
     *   <li>解密回调数据</li>
     *   <li>幂等性检查(pay_events表)</li>
     *   <li>更新订单状态</li>
     *   <li>发放积分(调用WalletService)</li>
     *   <li>记录回调事件</li>
     * </ol>
     *
     * <p>幂等性保证:
     * <ul>
     *   <li>通过pay_events表的(provider, event_id)唯一索引</li>
     *   <li>如果event_id已存在且processed=1,直接返回成功</li>
     *   <li>利用数据库唯一约束防止并发重复处理</li>
     * </ul>
     *
     * <p>事务处理:
     * <ul>
     *   <li>使用@Transactional保证数据一致性</li>
     *   <li>任何步骤失败都会回滚整个事务</li>
     * </ul>
     *
     * @param transaction 微信支付回调的Transaction对象(SDK已解密)
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果订单不存在或状态异常
     */
    void handleWechatCallback(Transaction transaction);
    
    /**
     * 处理微信支付回调通知(HttpServletRequest)
     *
     * <p>接收来自微信的原始回调请求，并进行验签、解密、处理
     *
     * @param request HTTP请求对象，包含微信回调的加密数据
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果验签失败或处理异常
     */
    void handleWechatCallback(HttpServletRequest request);

    /**
     * 取消未支付的订单
     *
     * <p>业务逻辑:
     * <ol>
     *   <li>查询订单是否存在</li>
     *   <li>验证订单是否属于当前用户</li>
     *   <li>验证订单状态是否为CREATED(未支付)</li>
     *   <li>更新订单状态为CANCELED</li>
     *   <li>记录取消时间</li>
     * </ol>
     *
     * <p>注意: 已支付的订单不能取消,只能申请退款
     *
     * @param userId 用户ID(从JWT获取)
     * @param orderNo 商户订单号
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果订单不存在、无权限或状态不允许取消
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 获取充值套餐列表
     *
     * <p>返回所有启用的充值套餐，按sortOrder排序
     *
     * @return 充值套餐列表
     */
    List<RechargeProductVO> getRechargeProducts();

    /**
     * 获取兑换规则列表
     *
     * <p>返回所有启用的兑换规则
     *
     * @return 兑换规则列表
     */
    List<ExchangeRuleVO> getExchangeRules();
}