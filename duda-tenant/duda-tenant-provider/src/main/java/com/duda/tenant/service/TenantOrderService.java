package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.entity.TenantOrder;

import java.util.List;

/**
 * 租户订单服务接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantOrderService extends IService<TenantOrder> {

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 创建的订单
     */
    TenantOrder createOrder(TenantOrder order);

    /**
     * 更新订单
     *
     * @param order 订单信息
     * @return 更新后的订单
     */
    TenantOrder updateOrder(TenantOrder order);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    TenantOrder getByOrderNo(String orderNo);

    /**
     * 根据租户ID查询订单列表
     *
     * @param tenantId 租户ID
     * @return 订单列表
     */
    List<TenantOrder> listByTenantId(Long tenantId);

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @param paymentNo 支付流水号
     * @return 是否成功
     */
    Boolean payOrder(Long orderId, String paymentMethod, String paymentNo);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 退款
     *
     * @param orderId 订单ID
     * @param reason 退款原因
     * @return 是否成功
     */
    Boolean refundOrder(Long orderId, String reason);

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    String generateOrderNo();
}
