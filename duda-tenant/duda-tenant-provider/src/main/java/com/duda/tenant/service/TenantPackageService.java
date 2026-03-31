package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.entity.TenantPackage;

import java.util.List;

/**
 * 租户套餐服务接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantPackageService extends IService<TenantPackage> {

    /**
     * 创建套餐
     *
     * @param tenantPackage 套餐信息
     * @return 创建的套餐
     */
    TenantPackage createPackage(TenantPackage tenantPackage);

    /**
     * 更新套餐
     *
     * @param tenantPackage 套餐信息
     * @return 更新后的套餐
     */
    TenantPackage updatePackage(TenantPackage tenantPackage);

    /**
     * 根据套餐编码查询套餐
     *
     * @param packageCode 套餐编码
     * @return 套餐信息
     */
    TenantPackage getByPackageCode(String packageCode);

    /**
     * 查询所有启用的套餐
     *
     * @return 套餐列表
     */
    List<TenantPackage> listActivePackages();

    /**
     * 根据套餐类型查询套餐
     *
     * @param packageType 套餐类型
     * @return 套餐列表
     */
    List<TenantPackage> listByPackageType(String packageType);

    /**
     * 启用/禁用套餐
     *
     * @param packageId 套餐ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    Boolean togglePackage(Long packageId, Boolean enabled);

    /**
     * 删除套餐
     *
     * @param packageId 套餐ID
     * @return 是否成功
     */
    Boolean deletePackage(Long packageId);

    /**
     * 计算套餐价格
     *
     * @param packageId 套餐ID
     * @param durationMonths 购买月数
     * @param billingCycle 计费周期（monthly/yearly）
     * @return 价格（分）
     */
    java.math.BigDecimal calculatePrice(Long packageId, Integer durationMonths, String billingCycle);
}
