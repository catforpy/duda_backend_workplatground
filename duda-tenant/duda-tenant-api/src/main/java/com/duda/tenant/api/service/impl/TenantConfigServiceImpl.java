package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantConfigDTO;
import com.duda.tenant.api.rpc.TenantConfigRpc;
import com.duda.tenant.api.service.TenantConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 租户配置Service实现类
 *
 * <p>负责处理租户配置相关的业务逻辑，通过Dubbo RPC调用duda-tenant-provider服务</p>
 *
 * @author Claude Code
 * @since 2026-03-31
 * @version 1.0.0
 */
@Slf4j
@Service
public class TenantConfigServiceImpl implements TenantConfigService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantConfigRpc tenantConfigRpc;

    @Override
    public TenantConfigDTO getConfig(Long tenantId, String configKey) {
        log.debug("查询租户配置: tenantId={}, configKey={}", tenantId, configKey);
        return tenantConfigRpc.getConfig(tenantId, configKey);
    }

    @Override
    public List<TenantConfigDTO> listConfigs(Long tenantId) {
        log.debug("查询租户配置列表: tenantId={}", tenantId);
        return tenantConfigRpc.listConfigs(tenantId);
    }

    @Override
    public Map<String, String> getConfigMap(Long tenantId) {
        log.debug("查询租户配置Map: tenantId={}", tenantId);
        return tenantConfigRpc.getConfigMap(tenantId);
    }

    @Override
    public TenantConfigDTO createConfig(TenantConfigDTO configDTO) {
        log.info("创建租户配置: tenantId={}, configKey={}",
                configDTO.getTenantId(), configDTO.getConfigKey());
        return tenantConfigRpc.createConfig(configDTO);
    }

    @Override
    public TenantConfigDTO updateConfig(TenantConfigDTO configDTO) {
        log.info("更新租户配置: configId={}", configDTO.getId());
        return tenantConfigRpc.updateConfig(configDTO);
    }

    @Override
    public Boolean deleteConfig(Long configId) {
        log.info("删除租户配置: configId={}", configId);
        return tenantConfigRpc.deleteConfig(configId);
    }
}
