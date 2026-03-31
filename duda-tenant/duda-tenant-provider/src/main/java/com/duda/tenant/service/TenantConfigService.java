package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.entity.TenantConfig;

import java.util.List;
import java.util.Map;

/**
 * 租户配置服务接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantConfigService extends IService<TenantConfig> {

    /**
     * 创建租户配置
     *
     * @param config 配置信息
     * @return 创建的配置
     */
    TenantConfig createConfig(TenantConfig config);

    /**
     * 更新租户配置
     *
     * @param config 配置信息
     * @return 更新后的配置
     */
    TenantConfig updateConfig(TenantConfig config);

    /**
     * 根据租户ID和配置键查询配置
     *
     * @param tenantId 租户ID
     * @param configKey 配置键
     * @return 配置信息
     */
    TenantConfig getByTenantIdAndKey(Long tenantId, String configKey);

    /**
     * 根据租户ID查询所有配置
     *
     * @param tenantId 租户ID
     * @return 配置列表
     */
    List<TenantConfig> listByTenantId(Long tenantId);

    /**
     * 根据租户ID查询配置Map
     *
     * @param tenantId 租户ID
     * @return 配置Map（key=configKey, value=configValue）
     */
    Map<String, String> getConfigMapByTenantId(Long tenantId);

    /**
     * 启用/禁用配置
     *
     * @param configId 配置ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    Boolean toggleConfig(Long configId, Boolean enabled);

    /**
     * 删除租户配置
     *
     * @param configId 配置ID
     * @return 是否成功
     */
    Boolean deleteConfig(Long configId);
}
