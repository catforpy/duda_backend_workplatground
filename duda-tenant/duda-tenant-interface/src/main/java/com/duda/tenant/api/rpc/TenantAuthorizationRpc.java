package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantAuthorizationDTO;

import java.util.List;

/**
 * 授权管理RPC接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantAuthorizationRpc {

    /**
     * 根据ID查询
     */
    TenantAuthorizationDTO getById(Long id);

    /**
     * 根据授权编码查询
     */
    TenantAuthorizationDTO getByAuthorizationCode(String authorizationCode);

    /**
     * 查询租户的所有授权
     */
    List<TenantAuthorizationDTO> listByTenant(Long tenantId);

    /**
     * 创建授权
     */
    TenantAuthorizationDTO create(TenantAuthorizationDTO dto);

    /**
     * 更新授权
     */
    TenantAuthorizationDTO update(TenantAuthorizationDTO dto);

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
