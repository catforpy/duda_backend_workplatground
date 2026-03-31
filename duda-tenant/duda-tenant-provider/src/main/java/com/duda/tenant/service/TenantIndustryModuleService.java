package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantIndustryModuleDTO;
import com.duda.tenant.entity.TenantIndustryModule;

import java.util.List;

/**
 * 行业模块表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantIndustryModuleService extends IService<TenantIndustryModule> {

    /**
     * 创建行业模块
     */
    TenantIndustryModule createModule(TenantIndustryModuleDTO dto);

    /**
     * 更新行业模块
     */
    TenantIndustryModule updateModule(TenantIndustryModuleDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantIndustryModuleDTO getModuleDTO(Long id);

    /**
     * 查询租户的行业模块
     */
    List<TenantIndustryModuleDTO> listByTenant(Long tenantId);

    /**
     * 启用模块
     */
    Boolean enable(Long id);

    /**
     * 禁用模块
     */
    Boolean disable(Long id);
}
