package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantMediaResourceDTO;
import com.duda.tenant.entity.TenantMediaResource;

import java.util.List;

/**
 * 媒体资源表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantMediaResourceService extends IService<TenantMediaResource> {

    /**
     * 创建媒体资源
     */
    TenantMediaResource createResource(TenantMediaResourceDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantMediaResourceDTO getResourceDTO(Long id);

    /**
     * 查询租户的媒体资源
     */
    List<TenantMediaResourceDTO> listByTenant(Long tenantId);

    /**
     * 查询租户的某种类型资源
     */
    List<TenantMediaResourceDTO> listByType(Long tenantId, String resourceType);
}
