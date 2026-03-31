package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.entity.TenantPackage;
import com.duda.tenant.mapper.TenantPackageMapper;
import com.duda.tenant.service.TenantPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户套餐服务实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Service
public class TenantPackageServiceImpl extends ServiceImpl<TenantPackageMapper, TenantPackage> implements TenantPackageService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantPackage createPackage(TenantPackage tenantPackage) {
        tenantPackage.setCreateTime(LocalDateTime.now());
        tenantPackage.setUpdateTime(LocalDateTime.now());
        if (tenantPackage.getIsActive() == null) {
            tenantPackage.setIsActive(1);
        }
        save(tenantPackage);
        log.info("创建套餐成功: packageId={}, packageCode={}",
                tenantPackage.getId(), tenantPackage.getPackageCode());
        return tenantPackage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantPackage updatePackage(TenantPackage tenantPackage) {
        tenantPackage.setUpdateTime(LocalDateTime.now());
        updateById(tenantPackage);
        log.info("更新套餐成功: packageId={}, packageCode={}",
                tenantPackage.getId(), tenantPackage.getPackageCode());
        return tenantPackage;
    }

    @Override
    public TenantPackage getByPackageCode(String packageCode) {
        LambdaQueryWrapper<TenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantPackage::getPackageCode, packageCode);
        return getOne(wrapper);
    }

    @Override
    public List<TenantPackage> listActivePackages() {
        LambdaQueryWrapper<TenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantPackage::getIsActive, 1)
                .orderByAsc(TenantPackage::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<TenantPackage> listByPackageType(String packageType) {
        LambdaQueryWrapper<TenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantPackage::getPackageType, packageType)
                .orderByAsc(TenantPackage::getSortOrder);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean togglePackage(Long packageId, Boolean enabled) {
        TenantPackage tenantPackage = getById(packageId);
        if (tenantPackage == null) {
            log.warn("启用/禁用套餐失败，套餐不存在: packageId={}", packageId);
            return false;
        }
        tenantPackage.setIsActive(enabled ? 1 : 0);
        tenantPackage.setUpdateTime(LocalDateTime.now());
        updateById(tenantPackage);
        log.info("启用/禁用套餐成功: packageId={}, enabled={}", packageId, enabled);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePackage(Long packageId) {
        boolean result = removeById(packageId);
        if (result) {
            log.info("删除套餐成功: packageId={}", packageId);
        } else {
            log.warn("删除套餐失败，套餐不存在: packageId={}", packageId);
        }
        return result;
    }

    @Override
    public BigDecimal calculatePrice(Long packageId, Integer durationMonths, String billingCycle) {
        TenantPackage tenantPackage = getById(packageId);
        if (tenantPackage == null) {
            log.warn("计算套餐价格失败，套餐不存在: packageId={}", packageId);
            return BigDecimal.ZERO;
        }

        BigDecimal basePrice = "yearly".equals(billingCycle)
                ? tenantPackage.getPriceYearly()
                : tenantPackage.getPriceMonthly();

        if (basePrice == null) {
            return BigDecimal.ZERO;
        }

        // 计算总价格
        return basePrice.multiply(new BigDecimal(durationMonths));
    }
}
