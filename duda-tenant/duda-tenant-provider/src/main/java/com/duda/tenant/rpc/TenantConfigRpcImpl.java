package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantConfigDTO;
import com.duda.tenant.api.rpc.TenantConfigRpc;
import com.duda.tenant.entity.TenantConfig;
import com.duda.tenant.service.TenantConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户配置RPC实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantConfigRpcImpl implements TenantConfigRpc {

    @Autowired
    private TenantConfigService tenantConfigService;

    @Override
    public TenantConfigDTO getConfig(Long tenantId, String configKey) {
        log.info("RPC调用: getConfig, tenantId={}, configKey={}", tenantId, configKey);
        TenantConfig config = tenantConfigService.getByTenantIdAndKey(tenantId, configKey);
        return entityToDto(config);
    }

    @Override
    public List<TenantConfigDTO> listConfigs(Long tenantId) {
        log.info("RPC调用: listConfigs, tenantId={}", tenantId);
        List<TenantConfig> configs = tenantConfigService.listByTenantId(tenantId);
        return configs.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getConfigMap(Long tenantId) {
        log.info("RPC调用: getConfigMap, tenantId={}", tenantId);
        return tenantConfigService.getConfigMapByTenantId(tenantId);
    }

    @Override
    public TenantConfigDTO createConfig(TenantConfigDTO configDTO) {
        log.info("RPC调用: createConfig, tenantId={}, configKey={}",
                configDTO.getTenantId(), configDTO.getConfigKey());
        TenantConfig config = dtoToEntity(configDTO);
        config = tenantConfigService.createConfig(config);
        return entityToDto(config);
    }

    @Override
    public TenantConfigDTO updateConfig(TenantConfigDTO configDTO) {
        log.info("RPC调用: updateConfig, configId={}", configDTO.getId());
        TenantConfig config = dtoToEntity(configDTO);
        config = tenantConfigService.updateConfig(config);
        return entityToDto(config);
    }

    @Override
    public Boolean deleteConfig(Long configId) {
        log.info("RPC调用: deleteConfig, configId={}", configId);
        return tenantConfigService.deleteConfig(configId);
    }

    /**
     * Entity转DTO
     */
    private TenantConfigDTO entityToDto(TenantConfig entity) {
        if (entity == null) {
            return null;
        }
        TenantConfigDTO dto = new TenantConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO转Entity
     */
    private TenantConfig dtoToEntity(TenantConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        TenantConfig entity = new TenantConfig();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
