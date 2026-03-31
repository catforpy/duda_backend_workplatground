package com.duda.tenant.rpc;

import com.duda.tenant.api.rpc.TenantUserRpc;
import com.duda.tenant.service.TenantUserDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 租户用户数据RPC实现
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
public class TenantUserRpcImpl implements TenantUserRpc {

    @Autowired
    private TenantUserDataService tenantUserDataService;

    @Override
    public void syncUserToTenant(Long userId, Long tenantId, String tenantCode) {
        log.info("【RPC】同步用户到租户: userId={}, tenantCode={}", userId, tenantCode);
        tenantUserDataService.syncUserToTenant(userId, tenantId, tenantCode);
    }

    @Override
    public void syncUserToAllTenants(Long userId) {
        log.info("【RPC】同步用户到所有租户: userId={}", userId);
        tenantUserDataService.syncUserToAllTenants(userId);
    }

    @Override
    public List<Map<String, Object>> getUsersFromTenant(Long tenantId, String tenantCode) {
        log.info("【RPC】查询租户用户: tenantCode={}", tenantCode);
        return tenantUserDataService.getUsersFromTenant(tenantId, tenantCode);
    }

    @Override
    public Map<String, Object> validateUserDataIsolation(Long userId) {
        log.info("【RPC】验证用户数据隔离: userId={}", userId);
        return tenantUserDataService.validateUserDataIsolation(userId);
    }
}
