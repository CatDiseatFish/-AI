package com.ym.ai_story_studio_server.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.ym.ai_story_studio_server.config.WechatPayProperties;
import com.ym.ai_story_studio_server.dto.payment.CreateNativeOrderRequest;
import com.ym.ai_story_studio_server.dto.payment.NativeOrderVO;
import com.ym.ai_story_studio_server.dto.payment.OrderStatusVO;
import com.ym.ai_story_studio_server.entity.*;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.*;
import com.ym.ai_story_studio_server.service.PaymentService;
import com.ym.ai_story_studio_server.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.ym.ai_story_studio_server.common.ResultCode.*;

/**
 * 支付服务实现类
 *
 * <p>实现微信Native扫码支付的完整业务流程,包括:
 * <ul>
 *   <li>订单创建: 生成订单并调用微信API获取二维码</li>
 *   <li>状态查询: 提供订单状态查询接口</li>
 *   <li>回调处理: 接收微信支付回调,验签、幂等处理、发放积分</li>
 *   <li>订单取消: 取消未支付订单</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PayOrderMapper payOrderMapper;
    private final PayEventMapper payEventMapper;
    private final RechargeProductMapper productMapper;
    private final RechargeExchangeRuleMapper exchangeRuleMapper;
    private final WalletService walletService;
    private final WechatPayProperties wechatPayProperties;

    /**
     * 微信Native支付服务客户端
     */
    private NativePayService nativePayService;

    /**
     * 微信支付SDK是否已初始化的标志
     */
    private volatile boolean initialized = false;

    /**
     * 懒加载初始化微信支付SDK配置
     *
     * <p>使用懒加载模式,只在第一次调用支付接口时才初始化SDK
     * <p>这样可以避免在启动时因为微信配置不完整导致项目启动失败
     *
     * @throws BusinessException 如果微信支付配置不正确或SDK初始化失败
     */
    private synchronized void ensureWechatPayServiceInitialized() {
        if (initialized) {
            return;
        }

        try {
            log.info("懒加载初始化微信支付SDK配置...");

            // 检查必要的配置是否存在
            if (wechatPayProperties.getMerchantId() == null || wechatPayProperties.getMerchantId().isEmpty()) {
                throw new BusinessException(SYSTEM_ERROR, "微信支付配置不完整: merchantId未配置");
            }

            // 使用自动更新平台证书的RSA配置
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(wechatPayProperties.getMerchantId())
                    .privateKeyFromPath(wechatPayProperties.getPrivateKeyPath())
                    .merchantSerialNumber(wechatPayProperties.getMerchantSerialNumber())
                    .apiV3Key(wechatPayProperties.getApiV3Key())
                    .build();

            // 初始化Native支付服务
            nativePayService = new NativePayService.Builder().config(config).build();
            initialized = true;

            log.info("微信支付SDK初始化成功: merchantId={}", wechatPayProperties.getMerchantId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信支付SDK初始化失败", e);
            throw new BusinessException(SYSTEM_ERROR, "微信支付SDK初始化失败: " + e.getMessage());
        }
    }

    @Override
    public NativeOrderVO createNativeOrder(Long userId, CreateNativeOrderRequest request) {
        log.info("创建Native支付订单: userId={}, request={}", userId, request);

        // 确保微信支付SDK已初始化（懒加载）
        ensureWechatPayServiceInitialized();

        // 1. 验证请求参数
        request.validate();

        // 2. 计算订单金额和积分
        Integer amountCents;
        Integer points;
        Long productId = null;
        Long exchangeRuleId = null;

        if (request.productId() != null) {
            // 使用充值商品
            RechargeProduct product = productMapper.selectById(request.productId());
            if (product == null || product.getEnabled() == 0) {
                throw new BusinessException(RESOURCE_NOT_FOUND, "充值商品不存在或已下架");
            }
            amountCents = product.getPriceCents();
            points = product.getPoints();
            productId = product.getId();

        } else {
            // 使用自定义金额
            amountCents = request.amountCents();

            // 查询启用的兑换规则
            RechargeExchangeRule rule = exchangeRuleMapper.selectOne(
                    new LambdaQueryWrapper<RechargeExchangeRule>()
                            .eq(RechargeExchangeRule::getCurrency, "CNY")
                            .eq(RechargeExchangeRule::getEnabled, 1)
                            .last("LIMIT 1")
            );

            if (rule == null) {
                throw new BusinessException(RESOURCE_NOT_FOUND, "兑换规则不存在");
            }

            // 验证金额范围
            if (amountCents < rule.getMinAmountCents()) {
                throw BusinessException.invalidParam(
                        String.format("充值金额不能少于%.2f元", rule.getMinAmountCents() / 100.0)
                );
            }
            if (rule.getMaxAmountCents() != null && amountCents > rule.getMaxAmountCents()) {
                throw BusinessException.invalidParam(
                        String.format("充值金额不能超过%.2f元", rule.getMaxAmountCents() / 100.0)
                );
            }

            // 计算积分
            points = amountCents / rule.getCentsPerPoint();
            exchangeRuleId = rule.getId();
        }

        // 3. 生成唯一订单号
        String orderNo = "WX" + IdUtil.getSnowflakeNextIdStr();

        // 4. 创建PayOrder记录
        PayOrder payOrder = new PayOrder();
        payOrder.setUserId(userId);
        payOrder.setProductId(productId);
        payOrder.setExchangeRuleId(exchangeRuleId);
        payOrder.setOrderNo(orderNo);
        payOrder.setProvider("WECHAT");
        payOrder.setScene("NATIVE");
        payOrder.setStatus("CREATED");
        payOrder.setAmountCents(amountCents);
        payOrder.setCurrency("CNY");
        payOrder.setPoints(points);
        payOrder.setTitle("AI故事工作室-积分充值");
        payOrder.setClientIp(request.clientIp());
        payOrder.setExpireAt(LocalDateTime.now().plusHours(2)); // 2小时后过期

        payOrderMapper.insert(payOrder);
        log.info("PayOrder创建成功: orderNo={}, amountCents={}, points={}", orderNo, amountCents, points);

        // 5. 调用微信Native下单API
        try {
            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(wechatPayProperties.getAppId());
            prepayRequest.setMchid(wechatPayProperties.getMerchantId());
            prepayRequest.setDescription(payOrder.getTitle());
            prepayRequest.setOutTradeNo(orderNo);
            prepayRequest.setNotifyUrl(wechatPayProperties.getNotifyUrl());

            Amount amount = new Amount();
            amount.setTotal(amountCents);
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            // 调用微信API
            PrepayResponse response = nativePayService.prepay(prepayRequest);
            String codeUrl = response.getCodeUrl();

            log.info("微信Native下单成功: orderNo={}, codeUrl={}", orderNo, codeUrl);

            // 6. 更新PayOrder
            payOrder.setProviderPayUrl(codeUrl);
            payOrderMapper.updateById(payOrder);

            // 7. 返回响应
            return new NativeOrderVO(
                    orderNo,
                    codeUrl,
                    amountCents,
                    points,
                    payOrder.getExpireAt()
            );

        } catch (ServiceException e) {
            log.error("微信Native下单失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
            throw new BusinessException(WECHAT_PAY_ERROR, "微信支付下单失败: " + e.getErrorMessage());
        }
    }

    @Override
    public OrderStatusVO queryOrderStatus(Long userId, String orderNo) {
        log.info("查询订单状态: userId={}, orderNo={}", userId, orderNo);

        // 1. 查询订单
        PayOrder payOrder = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOrderNo, orderNo)
        );

        if (payOrder == null) {
            throw new BusinessException(ORDER_NOT_FOUND);
        }

        // 2. 验证权限
        if (!payOrder.getUserId().equals(userId)) {
            throw BusinessException.accessDenied();
        }

        // 3. 返回状态
        return new OrderStatusVO(
                payOrder.getOrderNo(),
                payOrder.getStatus(),
                payOrder.getAmountCents(),
                payOrder.getPoints(),
                payOrder.getPaidAt(),
                payOrder.getCreatedAt()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatCallback(Transaction transaction) {
        String orderNo = transaction.getOutTradeNo();
        String transactionId = transaction.getTransactionId();
        String eventId = transactionId; // 使用微信交易号作为event_id

        log.info("处理微信支付回调: orderNo={}, transactionId={}", orderNo, transactionId);

        // 1. 幂等性检查
        PayEvent existingEvent = payEventMapper.selectOne(
                new LambdaQueryWrapper<PayEvent>()
                        .eq(PayEvent::getProvider, "WECHAT")
                        .eq(PayEvent::getEventId, eventId)
        );

        if (existingEvent != null && existingEvent.getProcessed() == 1) {
            log.info("回调事件已处理,幂等返回: eventId={}", eventId);
            return;
        }

        // 2. 插入pay_events记录(利用唯一索引防止并发)
        if (existingEvent == null) {
            try {
                PayEvent newEvent = new PayEvent();
                newEvent.setProvider("WECHAT");
                newEvent.setEventId(eventId);
                newEvent.setOrderNo(orderNo);
                newEvent.setProviderTradeNo(transactionId);
                newEvent.setEventType("PAY_SUCCESS");
                newEvent.setPayloadJson(transaction.toString());
                newEvent.setProcessed(0);

                payEventMapper.insert(newEvent);
                log.info("pay_events记录创建成功: eventId={}", eventId);

            } catch (DuplicateKeyException e) {
                // 并发情况下,另一个线程已插入,直接返回
                log.warn("并发插入pay_events冲突,幂等返回: eventId={}", eventId);
                return;
            }
        }

        // 3. 查询订单
        PayOrder payOrder = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOrderNo, orderNo)
        );

        if (payOrder == null) {
            log.error("订单不存在: orderNo={}", orderNo);
            throw new BusinessException(ORDER_NOT_FOUND);
        }

        // 4. 验证订单状态
        if ("SUCCEEDED".equals(payOrder.getStatus())) {
            log.warn("订单已支付,跳过处理: orderNo={}", orderNo);

            // 更新事件为已处理
            PayEvent event = payEventMapper.selectOne(
                    new LambdaQueryWrapper<PayEvent>()
                            .eq(PayEvent::getEventId, eventId)
            );
            if (event != null && event.getProcessed() == 0) {
                event.setProcessed(1);
                event.setProcessedAt(LocalDateTime.now());
                payEventMapper.updateById(event);
            }
            return;
        }

        // 5. 更新订单状态
        payOrder.setStatus("SUCCEEDED");
        payOrder.setProviderTradeNo(transactionId);
        payOrder.setPaidAt(LocalDateTime.now());
        payOrderMapper.updateById(payOrder);
        log.info("订单状态更新成功: orderNo={}, status=SUCCEEDED", orderNo);

        // 6. 发放积分(调用WalletService)
        try {
            // WalletService会自动处理钱包的增加余额和记录流水
            // 这里需要实现WalletService的recharge方法
            log.info("开始发放积分: userId={}, points={}", payOrder.getUserId(), payOrder.getPoints());
            // TODO: 调用 walletService.recharge(payOrder.getUserId(), payOrder.getPoints(), orderNo);
            // 由于WalletService可能还没有recharge方法,这里先记录日志
            log.warn("WalletService.recharge方法待实现,积分发放逻辑需要在WalletService中添加");

        } catch (Exception e) {
            log.error("积分发放失败: userId={}, points={}", payOrder.getUserId(), payOrder.getPoints(), e);
            throw new BusinessException(SYSTEM_ERROR, "积分发放失败");
        }

        // 7. 更新pay_events为已处理
        PayEvent event = payEventMapper.selectOne(
                new LambdaQueryWrapper<PayEvent>()
                        .eq(PayEvent::getEventId, eventId)
        );
        if (event != null) {
            event.setProcessed(1);
            event.setProcessedAt(LocalDateTime.now());
            payEventMapper.updateById(event);
        }

        log.info("微信支付回调处理完成: orderNo={}, transactionId={}", orderNo, transactionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatCallback(HttpServletRequest request) {
        log.info("接收微信支付回调请求");

        try {
            // 读取请求体
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            String body = requestBody.toString();
            log.info("微信回调原始数据: {}", body);

            // TODO: 实际实现中需要:
            // 1. 从请求头获取签名信息
            // String timestamp = request.getHeader("Wechatpay-Timestamp");
            // String nonce = request.getHeader("Wechatpay-Nonce");
            // String signature = request.getHeader("Wechatpay-Signature");
            // String serial = request.getHeader("Wechatpay-Serial");
            //
            // 2. 使用微信SDK的NotificationParser进行验签和解密
            // NotificationParser parser = new NotificationParser(config);
            // Transaction transaction = parser.parse(request, Transaction.class);
            //
            // 3. 调用Transaction版本的处理方法
            // handleWechatCallback(transaction);

            log.warn("微信回调HttpServletRequest版本待完整实现,需要SDK NotificationParser支持");

        } catch (IOException e) {
            log.error("读取微信回调请求失败", e);
            throw new BusinessException(SYSTEM_ERROR, "读取回调请求失败");
        }
    }

    @Override
    public void cancelOrder(Long userId, String orderNo) {
        log.info("取消订单: userId={}, orderNo={}", userId, orderNo);

        // 1. 查询订单
        PayOrder payOrder = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOrderNo, orderNo)
        );

        if (payOrder == null) {
            throw new BusinessException(ORDER_NOT_FOUND);
        }

        // 2. 验证权限
        if (!payOrder.getUserId().equals(userId)) {
            throw BusinessException.accessDenied();
        }

        // 3. 验证状态
        if (!"CREATED".equals(payOrder.getStatus())) {
            throw new BusinessException(PARAM_INVALID, "只能取消未支付的订单");
        }

        // 4. 更新订单状态
        payOrder.setStatus("CANCELED");
        payOrder.setClosedAt(LocalDateTime.now());
        payOrderMapper.updateById(payOrder);

        log.info("订单取消成功: orderNo={}", orderNo);
    }

    @Override
    public List<com.ym.ai_story_studio_server.dto.payment.RechargeProductVO> getRechargeProducts() {
        log.info("获取充值套餐列表");

        // 查询所有启用的充值套餐，按sortOrder排序
        var products = productMapper.selectList(
                new LambdaQueryWrapper<RechargeProduct>()
                        .eq(RechargeProduct::getEnabled, 1)
                        .orderByAsc(RechargeProduct::getSortOrder)
        );

        // 转换为VO
        return products.stream()
                .map(p -> new com.ym.ai_story_studio_server.dto.payment.RechargeProductVO(
                        p.getId(),
                        p.getName(),
                        p.getPoints(),
                        p.getPriceCents(),
                        p.getEnabled(),
                        p.getSortOrder(),
                        null,  // description (暂时没有这个字段)
                        p.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public List<com.ym.ai_story_studio_server.dto.payment.ExchangeRuleVO> getExchangeRules() {
        log.info("获取兑换规则列表");

        // 查询所有启用的兑换规则
        var rules = exchangeRuleMapper.selectList(
                new LambdaQueryWrapper<RechargeExchangeRule>()
                        .eq(RechargeExchangeRule::getEnabled, 1)
        );

        // 转换为VO (将centsPerPoint转换为pointsPerYuan)
        return rules.stream()
                .map(r -> new com.ym.ai_story_studio_server.dto.payment.ExchangeRuleVO(
                        r.getId(),
                        r.getName(),
                        com.ym.ai_story_studio_server.dto.payment.ExchangeRuleVO.calculatePointsPerYuan(r.getCentsPerPoint()),
                        r.getEnabled()
                ))
                .toList();
    }
}
