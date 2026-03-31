package com.duda.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.mq.MqTopicConstants;
import com.duda.common.mq.message.OrderCancelledMsg;
import com.duda.common.mq.message.OrderCreatedMsg;
import com.duda.common.mq.message.OrderPaidMsg;
import com.duda.common.redis.RedisUtils;
import com.duda.common.redis.idempotent.IdempotentHelper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.rocketmq.RocketMQUtils;
import com.duda.common.tenant.enums.OrderStatusEnum;
import com.duda.tenant.entity.TenantOrder;
import com.duda.tenant.mapper.TenantOrderMapper;
import com.duda.tenant.service.TenantOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 租户订单服务实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Service
public class TenantOrderServiceImpl extends ServiceImpl<TenantOrderMapper, TenantOrder> implements TenantOrderService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private IdempotentHelper idempotentHelper;

    @Resource
    private RedisDistributedLock redisDistributedLock;

    @Resource
    private RocketMQUtils rocketMQUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantOrder createOrder(TenantOrder order) {
        order.setOrderNo(generateOrderNo());
        order.setPaymentStatus(OrderStatusEnum.UNPAID.getCode());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        log.info("创建订单成功: orderId={}, orderNo={}, tenantId={}",
                order.getId(), order.getOrderNo(), order.getTenantId());

        // 发送订单创建消息到MQ（异步处理）
        try {
            OrderCreatedMsg createdMsg = new OrderCreatedMsg();
            createdMsg.setOrderId(order.getId());
            createdMsg.setOrderNo(order.getOrderNo());
            createdMsg.setTenantId(order.getTenantId());
            createdMsg.setTotalAmount(order.getOrderAmount());
            createdMsg.setCreateTime(LocalDateTime.now().toString());
            createdMsg.setOrderType("tenant_order");
            createdMsg.setTimeoutMinutes(30); // 30分钟超时

            rocketMQUtils.asyncSendWithKey(
                MqTopicConstants.ORDER_CREATE,
                createdMsg,
                RocketMQUtils.buildMessageKey("order-create", order.getId())
            );

            log.info("✅ 订单创建MQ消息发送成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("❌ 发送订单创建MQ消息失败: orderId={}", order.getId(), e);
            // 不影响订单创建流程
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantOrder updateOrder(TenantOrder order) {
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("更新订单成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        return order;
    }

    @Override
    public TenantOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<TenantOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantOrder::getOrderNo, orderNo);
        return getOne(wrapper);
    }

    @Override
    public List<TenantOrder> listByTenantId(Long tenantId) {
        LambdaQueryWrapper<TenantOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantOrder::getTenantId, tenantId)
                .orderByDesc(TenantOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(Long orderId, String paymentMethod, String paymentNo) {
        log.info("开始支付租户订单: orderId={}, paymentMethod={}, paymentNo={}",
                orderId, paymentMethod, paymentNo);

        // 参数校验
        if (orderId == null) {
            log.warn("订单ID不能为空");
            return false;
        }
        if (StrUtil.isBlank(paymentMethod)) {
            log.warn("支付方式不能为空: orderId={}", orderId);
            return false;
        }
        if (StrUtil.isBlank(paymentNo)) {
            log.warn("支付单号不能为空: orderId={}", orderId);
            return false;
        }

        // 幂等性检查：防止重复支付
        String idempotentKey = "pay:tenant:" + orderId + ":" + paymentNo;
        if (!idempotentHelper.checkAndSet(idempotentKey, 300)) {
            log.warn("支付请求重复，已处理: orderId={}, paymentNo={}", orderId, paymentNo);
            return false;
        }

        // 分布式锁：防止并发支付
        String lockKey = "pay:lock:tenant:" + orderId;
        String lockValue = String.valueOf(System.currentTimeMillis());
        boolean locked = false;
        try {
            locked = redisDistributedLock.tryLock(lockKey, lockValue, 30);
            if (!locked) {
                log.warn("获取支付锁失败，请稍后重试: orderId={}", orderId);
                return false;
            }

            TenantOrder order = getById(orderId);
            if (order == null) {
                log.warn("支付订单失败，订单不存在: orderId={}", orderId);
                return false;
            }

            if (!OrderStatusEnum.UNPAID.getCode().equals(order.getPaymentStatus())) {
                if (OrderStatusEnum.PAID.getCode().equals(order.getPaymentStatus())) {
                    log.warn("订单已支付，请勿重复支付: orderId={}, orderNo={}",
                            orderId, order.getOrderNo());
                } else {
                    log.warn("支付订单失败，订单状态不正确: orderId={}, status={}",
                            orderId, order.getPaymentStatus());
                }
                return false;
            }

            // TODO: 验证支付金额是否匹配
            // TODO: 调用第三方支付平台验证支付状态

            order.setPaymentStatus(OrderStatusEnum.PAID.getCode());
            order.setPaymentMethod(paymentMethod);
            order.setPaymentNo(paymentNo);
            order.setPaymentTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            updateById(order);

            log.info("支付订单成功: orderId={}, orderNo={}, paymentNo={}",
                    orderId, order.getOrderNo(), paymentNo);

            // 发送支付成功消息到MQ（异步处理后续业务）
            try {
                OrderPaidMsg paidMsg = new OrderPaidMsg();
                paidMsg.setOrderId(order.getId());
                paidMsg.setOrderNo(order.getOrderNo());
                paidMsg.setTenantId(order.getTenantId());
                paidMsg.setUserId(null); // TenantOrder没有userId字段
                paidMsg.setTotalAmount(order.getOrderAmount());
                paidMsg.setPaymentMethod(paymentMethod);
                paidMsg.setPaymentNo(paymentNo);
                paidMsg.setPaymentTime(LocalDateTime.now().toString());
                paidMsg.setOrderType("tenant_order");

                rocketMQUtils.asyncSendWithKey(
                    MqTopicConstants.ORDER_PAID,
                    paidMsg,
                    RocketMQUtils.buildMessageKey("order-paid", order.getId())
                );

                log.info("✅ 订单支付成功MQ消息发送成功: orderId={}", order.getId());
            } catch (Exception e) {
                log.error("❌ 发送订单支付MQ消息失败: orderId={}", order.getId(), e);
                // 不影响支付流程
            }

            return true;

        } finally {
            // 释放锁
            if (locked) {
                redisDistributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long orderId) {
        log.info("开始取消租户订单: orderId={}", orderId);

        TenantOrder order = getById(orderId);
        if (order == null) {
            log.warn("取消订单失败，订单不存在: orderId={}", orderId);
            return false;
        }

        if (!OrderStatusEnum.UNPAID.getCode().equals(order.getPaymentStatus())) {
            log.warn("取消订单失败，订单状态不允许取消: orderId={}, status={}",
                    orderId, order.getPaymentStatus());
            return false;
        }

        order.setPaymentStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        log.info("取消订单成功: orderId={}, orderNo={}", orderId, order.getOrderNo());

        // 发送订单取消消息到MQ（异步处理库存释放等）
        try {
            OrderCancelledMsg cancelledMsg = new OrderCancelledMsg();
            cancelledMsg.setOrderId(order.getId());
            cancelledMsg.setOrderNo(order.getOrderNo());
            cancelledMsg.setTenantId(order.getTenantId());
            cancelledMsg.setReason("用户取消");
            cancelledMsg.setCancelTime(LocalDateTime.now().toString());
            cancelledMsg.setOrderType("tenant_order");
            cancelledMsg.setNeedRefund(false);

            rocketMQUtils.asyncSendWithKey(
                MqTopicConstants.ORDER_CANCEL,
                cancelledMsg,
                RocketMQUtils.buildMessageKey("order-cancel", order.getId())
            );

            log.info("✅ 订单取消MQ消息发送成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("❌ 发送订单取消MQ消息失败: orderId={}", order.getId(), e);
            // 不影响取消流程
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refundOrder(Long orderId, String reason) {
        log.info("开始退款租户订单: orderId={}, reason={}", orderId, reason);

        TenantOrder order = getById(orderId);
        if (order == null) {
            log.warn("退款失败，订单不存在: orderId={}", orderId);
            return false;
        }

        if (!OrderStatusEnum.PAID.getCode().equals(order.getPaymentStatus())) {
            log.warn("退款失败，订单状态不允许退款: orderId={}, status={}",
                    orderId, order.getPaymentStatus());
            return false;
        }

        order.setPaymentStatus(OrderStatusEnum.REFUNDED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        log.info("订单退款成功: orderId={}, orderNo={}, reason={}",
                orderId, order.getOrderNo(), reason);

        // 发送订单取消消息到MQ（退款也是取消的一种，复用取消消息）
        try {
            OrderCancelledMsg cancelledMsg = new OrderCancelledMsg();
            cancelledMsg.setOrderId(order.getId());
            cancelledMsg.setOrderNo(order.getOrderNo());
            cancelledMsg.setTenantId(order.getTenantId());
            cancelledMsg.setReason(reason);
            cancelledMsg.setCancelTime(LocalDateTime.now().toString());
            cancelledMsg.setOrderType("tenant_order");
            cancelledMsg.setNeedRefund(true);

            rocketMQUtils.asyncSendWithKey(
                MqTopicConstants.ORDER_CANCEL,
                cancelledMsg,
                RocketMQUtils.buildMessageKey("order-refund", order.getId())
            );

            log.info("✅ 订单退款MQ消息发送成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("❌ 发送订单退款MQ消息失败: orderId={}", order.getId(), e);
            // 不影响退款流程
        }

        return true;
    }

    @Override
    public String generateOrderNo() {
        // 生成格式：TO + yyyyMMdd + 6位随机数
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        int randomNum = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TO" + dateStr + randomNum;
    }
}
