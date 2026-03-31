package com.duda.tenant.api.rpc;

import java.util.List;
import java.util.Map;

/**
 * 租户订单同步RPC接口
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
public interface TenantOrderSyncRpc {

    /**
     * 同步所有租户订单到duda_nexus
     */
    void syncAllTenantOrders();

    /**
     * 创建跨租户订单视图
     */
    void createCrossTenantView();

    /**
     * 查询同步后的订单
     */
    List<Map<String, Object>> getSyncedOrders();

    /**
     * 查询跨租户视图
     */
    List<Map<String, Object>> getCrossTenantView();
}
