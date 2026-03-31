package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.TenantDTO;
import com.duda.tenant.api.dto.TenantCheckDTO;

/**
 * 租户Service接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantService {

    /**
     * 根据ID查询租户
     *
     * @param id 租户ID
     * @return 租户DTO
     */
    TenantDTO getTenantById(Long id);

    /**
     * 根据租户编码查询租户
     *
     * @param tenantCode 租户编码
     * @return 租户DTO
     */
    TenantDTO getTenantByCode(String tenantCode);

    /**
     * 创建租户
     *
     * @param tenantDTO 租户DTO
     * @return 租户DTO
     */
    TenantDTO createTenant(TenantDTO tenantDTO);

    /**
     * 更新租户
     *
     * @param tenantDTO 租户DTO
     * @return 租户DTO
     */
    TenantDTO updateTenant(TenantDTO tenantDTO);

    /**
     * 暂停租户
     *
     * @param id 租户ID
     * @return 是否成功
     */
    Boolean suspendTenant(Long id);

    /**
     * 激活租户
     *
     * @param id 租户ID
     * @return 是否成功
     */
    Boolean activateTenant(Long id);

    /**
     * 更新租户套餐
     *
     * @param id 租户ID
     * @param packageId 套餐ID
     * @return 是否成功
     */
    Boolean updatePackage(Long id, Long packageId);

    /**
     * 检查租户是否有效
     *
     * @param id 租户ID
     * @return 检查结果
     */
    TenantCheckDTO checkTenantValid(Long id);
}
