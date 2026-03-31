package com.duda.tenant.rpc;

import com.duda.tenant.api.rpc.CrossTenantTestDataRpc;
import com.duda.tenant.service.CrossTenantTestDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 跨租户测试数据RPC实现
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "DUDA_TENANT_GROUP",
    timeout = 30000  // 30秒超时，因为涉及跨租户查询
)
public class CrossTenantTestDataRpcImpl implements CrossTenantTestDataRpc {

    @Autowired
    private CrossTenantTestDataService crossTenantTestDataService;

    @Override
    public void initProductsForTenant(Long tenantId, String tenantCode) {
        log.info("【RPC】初始化租户商品: tenantId={}, tenantCode={}", tenantId, tenantCode);
        crossTenantTestDataService.initProductsForTenant(tenantId, tenantCode);
    }

    @Override
    public void createOrderForTenant(Long tenantId, String tenantCode, Long userId,
                                    java.math.BigDecimal amount, String status) {
        log.info("【RPC】创建租户订单: tenantId={}, userId={}, amount={}", tenantId, userId, amount);
        crossTenantTestDataService.createOrderForTenant(tenantId, tenantCode, userId, amount, status);
    }

    @Override
    public List<Map<String, Object>> getOrdersFromTenant(Long tenantId, String tenantCode) {
        log.info("【RPC】查询租户订单: tenantId={}, tenantCode={}", tenantId, tenantCode);
        return crossTenantTestDataService.getOrdersFromTenant(tenantId, tenantCode);
    }

    @Override
    public List<Map<String, Object>> getProductsFromTenant(Long tenantId, String tenantCode) {
        log.info("【RPC】查询租户商品: tenantId={}, tenantCode={}", tenantId, tenantCode);
        return crossTenantTestDataService.getProductsFromTenant(tenantId, tenantCode);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getUserOrdersAcrossTenants(Long userId) {
        log.info("【RPC】跨租户查询用户订单: userId={}", userId);
        return crossTenantTestDataService.getUserOrdersAcrossTenants(userId);
    }

    @Override
    public Map<String, Object> runFullCrossTenantTest(Long userId) {
        log.info("【RPC】执行完整跨租户测试: userId={}", userId);
        return crossTenantTestDataService.runFullCrossTenantTest(userId);
    }
}
