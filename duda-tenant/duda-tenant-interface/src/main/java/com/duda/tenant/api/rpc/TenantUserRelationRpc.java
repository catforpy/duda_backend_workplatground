package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantUserRelationDTO;

import java.util.List;

/**
 * 租户用户关系RPC接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantUserRelationRpc {

    /**
     * 用户加入小程序（创建用户-租户关联）
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @param roleCode 角色编码（TENANT_ADMIN/TENANT_USER）
     * @param isPrimary 是否为主租户（1=是，0=否）
     * @return 创建的关系DTO
     */
    TenantUserRelationDTO joinTenant(Long userId, Long tenantId, String roleCode, Integer isPrimary);

    /**
     * 查询用户的小程序列表
     *
     * @param userId 用户ID
     * @return 小程序列表
     */
    List<TenantUserRelationDTO> getTenantsByUserId(Long userId);

    /**
     * 查询小程序的用户列表
     *
     * @param tenantId 租户ID
     * @return 用户列表
     */
    List<TenantUserRelationDTO> getUsersByTenantId(Long tenantId);

    /**
     * 检查用户是否已关联某小程序
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 是否已关联
     */
    Boolean checkRelation(Long userId, Long tenantId);

    /**
     * 用户离开小程序（删除关联）
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean leaveTenant(Long userId, Long tenantId);

    /**
     * 根据ID查询关系
     *
     * @param id 关系ID
     * @return 关系DTO
     */
    TenantUserRelationDTO getRelationById(Long id);

    /**
     * 更新用户状态
     *
     * @param id 关系ID
     * @param status 状态（active/inactive）
     * @return 是否成功
     */
    Boolean updateStatus(Long id, String status);
}
