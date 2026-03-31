package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.entity.Tenant;

/**
 * 租户服务接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantService extends IService<Tenant> {

    /**
     * 创建租户
     *
     * @param tenant 租户信息
     * @return 创建的租户
     */
    Tenant createTenant(Tenant tenant);

    /**
     * 更新租户信息
     *
     * @param tenant 租户信息
     * @return 更新后的租户
     */
    Tenant updateTenant(Tenant tenant);

    /**
     * 根据租户编码查询租户
     *
     * @param tenantCode 租户编码
     * @return 租户信息
     */
    Tenant getByTenantCode(String tenantCode);

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
     * 检查租户是否有效
     *
     * @param tenantId 租户ID
     * @return 是否有效
     */
    Boolean isValidTenant(Long tenantId);

    /**
     * 更新租户套餐
     *
     * @param tenantId 租户ID
     * @param packageId 套餐ID
     * @return 是否成功
     */
    Boolean updatePackage(Long tenantId, Long packageId);

    /**
     * 检查租户配额
     *
     * @param tenantId 租户ID
     * @param quotaType 配额类型（user_count/storage_size/api_calls）
     * @return 是否超限
     */
    Boolean checkQuota(Long tenantId, String quotaType);
}
