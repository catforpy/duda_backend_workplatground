package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.TenantPackageDTO;

import java.util.List;

/**
 * 租户套餐Service接口
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantPackageService {

    /**
     * 查询所有套餐
     *
     * @return 套餐列表
     */
    List<TenantPackageDTO> listAll();

    /**
     * 根据ID查询套餐
     *
     * @param id 套餐ID
     * @return 套餐DTO
     */
    TenantPackageDTO getById(Long id);

    /**
     * 创建套餐
     *
     * @param packageDTO 套餐DTO
     * @return 创建的套餐
     */
    TenantPackageDTO create(TenantPackageDTO packageDTO);

    /**
     * 更新套餐
     *
     * @param packageDTO 套餐DTO
     * @return 更新后的套餐
     */
    TenantPackageDTO update(TenantPackageDTO packageDTO);

    /**
     * 删除套餐
     *
     * @param id 套餐ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 启用/禁用套餐
     *
     * @param id 套餐ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    Boolean toggleActive(Long id, Boolean enabled);

    /**
     * 根据套餐类型查询套餐
     *
     * @param packageType 套餐类型（PLATFORM/TENANT）
     * @return 套餐列表
     */
    List<TenantPackageDTO> listByPackageType(String packageType);

    /**
     * 根据租户ID查询套餐
     *
     * @param tenantId 租户ID
     * @return 套餐列表
     */
    List<TenantPackageDTO> listByTenantId(Long tenantId);

    /**
     * 根据目标用户类型查询套餐
     *
     * @param targetUserType 目标用户类型（ALL/RENTAL/PARTNER）
     * @return 套餐列表
     */
    List<TenantPackageDTO> listByTargetUserType(String targetUserType);

    /**
     * 复制套餐（用于快速创建新套餐）
     *
     * @param id 原套餐ID
     * @param newPackageCode 新套餐编码
     * @param newPackageName 新套餐名称
     * @return 新套餐
     */
    TenantPackageDTO copy(Long id, String newPackageCode, String newPackageName);
}
