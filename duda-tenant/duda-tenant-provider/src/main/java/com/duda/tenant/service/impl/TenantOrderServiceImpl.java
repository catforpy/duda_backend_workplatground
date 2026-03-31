package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.common.tenant.enums.OrderStatusEnum;
import com.duda.tenant.entity.TenantOrder;
import com.duda.tenant.mapper.TenantOrderMapper;
import com.duda.tenant.service.TenantOrderService;
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
        TenantOrder order = getById(orderId);
        if (order == null) {
            log.warn("支付订单失败，订单不存在: orderId={}", orderId);
            return false;
        }

        if (!OrderStatusEnum.UNPAID.getCode().equals(order.getPaymentStatus())) {
            log.warn("支付订单失败，订单状态不正确: orderId={}, status={}",
                    orderId, order.getPaymentStatus());
            return false;
        }

        order.setPaymentStatus(OrderStatusEnum.PAID.getCode());
        order.setPaymentMethod(paymentMethod);
        order.setPaymentNo(paymentNo);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        log.info("支付订单成功: orderId={}, orderNo={}, paymentNo={}",
                orderId, order.getOrderNo(), paymentNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long orderId) {
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
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refundOrder(Long orderId, String reason) {
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
