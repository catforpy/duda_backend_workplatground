package com.duda.tenant.rpc;

import com.duda.tenant.api.rpc.TenantOrderSyncRpc;
import com.duda.tenant.service.TenantOrderSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 租户订单同步RPC实现
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "DUDA_TENANT_GROUP",
    timeout = 30000
)
public class TenantOrderSyncRpcImpl implements TenantOrderSyncRpc {

    @Autowired
    private TenantOrderSyncService tenantOrderSyncService;

    @Override
    public void syncAllTenantOrders() {
        log.info("【RPC】同步所有租户订单");
        tenantOrderSyncService.syncAllTenantOrders();
    }

    @Override
    public void createCrossTenantView() {
        log.info("【RPC】创建跨租户订单视图");
        tenantOrderSyncService.createCrossTenantView();
    }

    @Override
    public List<Map<String, Object>> getSyncedOrders() {
        log.info("【RPC】查询同步订单");
        return tenantOrderSyncService.getSyncedOrders();
    }

    @Override
    public List<Map<String, Object>> getCrossTenantView() {
        log.info("【RPC】查询跨租户视图");
        return tenantOrderSyncService.getCrossTenantView();
    }
}
