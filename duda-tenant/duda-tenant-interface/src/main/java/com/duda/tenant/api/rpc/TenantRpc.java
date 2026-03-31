package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.*;

import java.util.List;

/**
 * 租户RPC接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantRpc {

    /**
     * 根据ID查询租户
     *
     * @param tenantId 租户ID
     * @return 租户DTO
     */
    TenantDTO getTenantById(Long tenantId);

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
     * @return 创建的租户DTO
     */
    TenantDTO createTenant(TenantDTO tenantDTO);

    /**
     * 更新租户
     *
     * @param tenantDTO 租户DTO
     * @return 更新后的租户DTO
     */
    TenantDTO updateTenant(TenantDTO tenantDTO);

    /**
     * 检查租户是否有效
     *
     * @param tenantId 租户ID
     * @return 检查结果DTO
     */
    TenantCheckDTO checkTenantValid(Long tenantId);

    /**
     * 检查租户配额
     *
     * @param tenantId 租户ID
     * @param quotaType 配额类型（user_count/storage_size/api_calls）
     * @return 配额检查结果DTO
     */
    QuotaCheckDTO checkQuota(Long tenantId, String quotaType);

    /**
     * 更新租户套餐
     *
     * @param tenantId 租户ID
     * @param packageId 套餐ID
     * @return 是否成功
     */
    Boolean updatePackage(Long tenantId, Long packageId);

    /**
     * 暂停租户
     *
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean suspendTenant(Long tenantId);

    /**
     * 激活租户
     *
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean activateTenant(Long tenantId);

    /**
     * 根据租户ID查询租户配置Map
     *
     * @param tenantId 租户ID
     * @return 配置Map
     */
    java.util.Map<String, String> getTenantConfigMap(Long tenantId);

    /**
     * 记录API调用
     *
     * @param tenantId 租户ID
     * @return 是否超限
     */
    Boolean recordApiCall(Long tenantId);
}
