package com.duda.tenant.api.fallback;

import com.duda.tenant.api.dto.TenantOrderDTO;
import com.duda.tenant.api.rpc.TenantOrderRpc;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 租户订单RPC降级实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantOrderRpcFallback implements TenantOrderRpc {

    @Override
    public TenantOrderDTO getOrderById(Long orderId) {
        log.error("TenantOrderRpc.getOrderById降级: orderId={}", orderId);
        return null;
    }

    @Override
    public TenantOrderDTO getOrderByNo(String orderNo) {
        log.error("TenantOrderRpc.getOrderByNo降级: orderNo={}", orderNo);
        return null;
    }

    @Override
    public List<TenantOrderDTO> listOrdersByTenantId(Long tenantId) {
        log.error("TenantOrderRpc.listOrdersByTenantId降级: tenantId={}", tenantId);
        return Collections.emptyList();
    }

    @Override
    public TenantOrderDTO createOrder(TenantOrderDTO orderDTO) {
        log.error("TenantOrderRpc.createOrder降级: tenantId={}", orderDTO.getTenantId());
        return null;
    }

    @Override
    public Boolean payOrder(Long orderId, String paymentMethod, String paymentNo) {
        log.error("TenantOrderRpc.payOrder降级: orderId={}, paymentMethod={}", orderId, paymentMethod);
        return false;
    }
}
