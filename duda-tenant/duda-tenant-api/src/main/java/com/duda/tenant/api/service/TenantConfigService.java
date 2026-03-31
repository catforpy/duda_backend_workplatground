package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.TenantConfigDTO;

import java.util.List;
import java.util.Map;

/**
 * 租户配置Service接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantConfigService {

    /**
     * 根据租户ID和配置键查询配置
     *
     * @param tenantId 租户ID
     * @param configKey 配置键
     * @return 配置DTO
     */
    TenantConfigDTO getConfig(Long tenantId, String configKey);

    /**
     * 根据租户ID查询所有配置
     *
     * @param tenantId 租户ID
     * @return 配置列表
     */
    List<TenantConfigDTO> listConfigs(Long tenantId);

    /**
     * 根据租户ID查询配置Map
     *
     * @param tenantId 租户ID
     * @return 配置Map
     */
    Map<String, String> getConfigMap(Long tenantId);

    /**
     * 创建配置
     *
     * @param configDTO 配置DTO
     * @return 创建后的配置DTO
     */
    TenantConfigDTO createConfig(TenantConfigDTO configDTO);

    /**
     * 更新配置
     *
     * @param configDTO 配置DTO
     * @return 更新后的配置DTO
     */
    TenantConfigDTO updateConfig(TenantConfigDTO configDTO);

    /**
     * 删除配置
     *
     * @param configId 配置ID
     * @return 是否成功
     */
    Boolean deleteConfig(Long configId);
}
