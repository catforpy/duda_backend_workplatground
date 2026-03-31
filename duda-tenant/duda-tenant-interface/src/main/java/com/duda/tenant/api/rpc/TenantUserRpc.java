package com.duda.tenant.api.rpc;

import java.util.List;
import java.util.Map;

/**
 * 租户用户数据RPC接口
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
public interface TenantUserRpc {

    /**
     * 同步用户到指定租户
     */
    void syncUserToTenant(Long userId, Long tenantId, String tenantCode);

    /**
     * 同步用户到所有租户
     */
    void syncUserToAllTenants(Long userId);

    /**
     * 查询租户中的用户
     */
    List<Map<String, Object>> getUsersFromTenant(Long tenantId, String tenantCode);

    /**
     * 验证用户数据隔离
     */
    Map<String, Object> validateUserDataIsolation(Long userId);
}
