package com.duda.tenant.api.rpc;

import com.duda.tenant.api.dto.TenantPackageDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 租户套餐RPC接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantPackageRpc {

    /**
     * 根据ID查询套餐
     *
     * @param packageId 套餐ID
     * @return 套餐DTO
     */
    TenantPackageDTO getPackageById(Long packageId);

    /**
     * 根据套餐编码查询套餐
     *
     * @param packageCode 套餐编码
     * @return 套餐DTO
     */
    TenantPackageDTO getPackageByCode(String packageCode);

    /**
     * 查询所有启用的套餐
     *
     * @return 套餐列表
     */
    List<TenantPackageDTO> listActivePackages();

    /**
     * 根据套餐类型查询套餐
     *
     * @param packageType 套餐类型
     * @return 套餐列表
     */
    List<TenantPackageDTO> listPackagesByType(String packageType);

    /**
     * 根据套餐类型查询套餐（别名方法）
     *
     * @param packageType 套餐类型
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
     * @param targetUserType 目标用户类型
     * @return 套餐列表
     */
    List<TenantPackageDTO> listByTargetUserType(String targetUserType);

    /**
     * 创建套餐
     *
     * @param packageDTO 套餐DTO
     * @return 创建的套餐
     */
    TenantPackageDTO createPackage(TenantPackageDTO packageDTO);

    /**
     * 更新套餐
     *
     * @param packageDTO 套餐DTO
     * @return 更新后的套餐
     */
    TenantPackageDTO updatePackage(TenantPackageDTO packageDTO);

    /**
     * 删除套餐
     *
     * @param packageId 套餐ID
     * @return 是否成功
     */
    Boolean deletePackage(Long packageId);

    /**
     * 启用/禁用套餐
     *
     * @param packageId 套餐ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    Boolean togglePackage(Long packageId, Boolean enabled);

    /**
     * 复制套餐
     *
     * @param packageId 原套餐ID
     * @param newPackageCode 新套餐编码
     * @param newPackageName 新套餐名称
     * @return 新套餐
     */
    TenantPackageDTO copyPackage(Long packageId, String newPackageCode, String newPackageName);

    /**
     * 计算套餐价格
     *
     * @param packageId 套餐ID
     * @param durationMonths 购买月数
     * @param billingCycle 计费周期（monthly/yearly）
     * @return 价格
     */
    BigDecimal calculatePrice(Long packageId, Integer durationMonths, String billingCycle);
}
