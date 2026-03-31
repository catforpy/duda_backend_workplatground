package com.duda.tenant.api.fallback;

import com.duda.tenant.api.dto.TenantConfigDTO;
import com.duda.tenant.api.rpc.TenantConfigRpc;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 租户配置RPC降级实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantConfigRpcFallback implements TenantConfigRpc {

    @Override
    public TenantConfigDTO getConfig(Long tenantId, String configKey) {
        log.error("TenantConfigRpc.getConfig降级: tenantId={}, configKey={}", tenantId, configKey);
        return null;
    }

    @Override
    public List<TenantConfigDTO> listConfigs(Long tenantId) {
        log.error("TenantConfigRpc.listConfigs降级: tenantId={}", tenantId);
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getConfigMap(Long tenantId) {
        log.error("TenantConfigRpc.getConfigMap降级: tenantId={}", tenantId);
        return Collections.emptyMap();
    }

    @Override
    public TenantConfigDTO createConfig(TenantConfigDTO configDTO) {
        log.error("TenantConfigRpc.createConfig降级: tenantId={}", configDTO.getTenantId());
        return null;
    }

    @Override
    public TenantConfigDTO updateConfig(TenantConfigDTO configDTO) {
        log.error("TenantConfigRpc.updateConfig降级: configId={}", configDTO.getId());
        return null;
    }

    @Override
    public Boolean deleteConfig(Long configId) {
        log.error("TenantConfigRpc.deleteConfig降级: configId={}", configId);
        return false;
    }
}
