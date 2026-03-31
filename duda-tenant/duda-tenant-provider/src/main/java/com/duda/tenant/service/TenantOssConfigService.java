package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantOssConfigDTO;
import com.duda.tenant.entity.TenantOssConfig;

/**
 * OSS配置表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantOssConfigService extends IService<TenantOssConfig> {

    /**
     * 创建OSS配置
     */
    TenantOssConfig createConfig(TenantOssConfigDTO dto);

    /**
     * 更新OSS配置
     */
    TenantOssConfig updateConfig(TenantOssConfigDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantOssConfigDTO getConfigDTO(Long id);

    /**
     * 查询租户的OSS配置
     */
    TenantOssConfigDTO getTenantConfig(Long tenantId);
}
