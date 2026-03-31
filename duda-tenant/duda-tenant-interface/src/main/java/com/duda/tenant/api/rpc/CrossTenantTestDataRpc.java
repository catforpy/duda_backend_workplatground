package com.duda.tenant.api.rpc;

import java.util.List;
import java.util.Map;

/**
 * 跨租户测试数据RPC接口
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
public interface CrossTenantTestDataRpc {

    /**
     * 在指定租户的schema中初始化商品数据
     */
    void initProductsForTenant(Long tenantId, String tenantCode);

    /**
     * 在指定租户的schema中为用户创建订单
     */
    void createOrderForTenant(Long tenantId, String tenantCode, Long userId,
                              java.math.BigDecimal amount, String status);

    /**
     * 查询指定租户的订单
     */
    List<Map<String, Object>> getOrdersFromTenant(Long tenantId, String tenantCode);

    /**
     * 查询指定租户的商品
     */
    List<Map<String, Object>> getProductsFromTenant(Long tenantId, String tenantCode);

    /**
     * 跨租户查询用户的所有订单
     */
    Map<String, List<Map<String, Object>>> getUserOrdersAcrossTenants(Long userId);

    /**
     * 完整的跨租户业务测试
     */
    Map<String, Object> runFullCrossTenantTest(Long userId);
}
