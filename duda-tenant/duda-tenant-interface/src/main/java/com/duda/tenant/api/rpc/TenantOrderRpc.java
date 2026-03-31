package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantOrderDTO;

import java.util.List;

/**
 * 租户订单RPC接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantOrderRpc {

    /**
     * 根据ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单DTO
     */
    TenantOrderDTO getOrderById(Long orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单DTO
     */
    TenantOrderDTO getOrderByNo(String orderNo);

    /**
     * 根据租户ID查询订单列表
     *
     * @param tenantId 租户ID
     * @return 订单列表
     */
    List<TenantOrderDTO> listOrdersByTenantId(Long tenantId);

    /**
     * 创建订单
     *
     * @param orderDTO 订单DTO
     * @return 创建的订单DTO
     */
    TenantOrderDTO createOrder(TenantOrderDTO orderDTO);

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @param paymentNo 支付流水号
     * @return 是否成功
     */
    Boolean payOrder(Long orderId, String paymentMethod, String paymentNo);
}
