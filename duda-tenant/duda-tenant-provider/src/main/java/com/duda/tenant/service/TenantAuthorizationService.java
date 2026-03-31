package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantAuthorizationDTO;
import com.duda.tenant.entity.TenantAuthorization;

import java.util.List;

/**
 * 授权管理表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantAuthorizationService extends IService<TenantAuthorization> {

    /**
     * 创建授权
     */
    TenantAuthorization createAuthorization(TenantAuthorizationDTO dto);

    /**
     * 更新授权
     */
    TenantAuthorization updateAuthorization(TenantAuthorizationDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantAuthorizationDTO getAuthorizationDTO(Long id);

    /**
     * 根据授权编码查询
     */
    TenantAuthorizationDTO getByAuthorizationCode(String authorizationCode);

    /**
     * 查询租户的所有授权
     */
    List<TenantAuthorizationDTO> listByTenant(Long tenantId);

    /**
     * 暂停授权
     */
    Boolean suspend(Long id);

    /**
     * 激活授权
     */
    Boolean activate(Long id);

    /**
     * 终止授权
     */
    Boolean terminate(Long id);
}
