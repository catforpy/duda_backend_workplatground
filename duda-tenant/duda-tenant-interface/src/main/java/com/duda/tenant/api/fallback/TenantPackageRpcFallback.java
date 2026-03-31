package com.duda.tenant.api.fallback;

import com.duda.tenant.api.dto.TenantPackageDTO;
import com.duda.tenant.api.rpc.TenantPackageRpc;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 租户套餐RPC降级实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantPackageRpcFallback implements TenantPackageRpc {

    @Override
    public TenantPackageDTO getPackageById(Long packageId) {
        log.error("TenantPackageRpc.getPackageById降级: packageId={}", packageId);
        return null;
    }

    @Override
    public TenantPackageDTO getPackageByCode(String packageCode) {
        log.error("TenantPackageRpc.getPackageByCode降级: packageCode={}", packageCode);
        return null;
    }

    @Override
    public List<TenantPackageDTO> listActivePackages() {
        log.error("TenantPackageRpc.listActivePackages降级");
        return Collections.emptyList();
    }

    @Override
    public List<TenantPackageDTO> listPackagesByType(String packageType) {
        log.error("TenantPackageRpc.listPackagesByType降级: packageType={}", packageType);
        return Collections.emptyList();
    }

    @Override
    public List<TenantPackageDTO> listByPackageType(String packageType) {
        log.error("TenantPackageRpc.listByPackageType降级: packageType={}", packageType);
        return Collections.emptyList();
    }

    @Override
    public List<TenantPackageDTO> listByTenantId(Long tenantId) {
        log.error("TenantPackageRpc.listByTenantId降级: tenantId={}", tenantId);
        return Collections.emptyList();
    }

    @Override
    public List<TenantPackageDTO> listByTargetUserType(String targetUserType) {
        log.error("TenantPackageRpc.listByTargetUserType降级: targetUserType={}", targetUserType);
        return Collections.emptyList();
    }

    @Override
    public TenantPackageDTO createPackage(TenantPackageDTO packageDTO) {
        log.error("TenantPackageRpc.createPackage降级: packageCode={}", packageDTO.getPackageCode());
        return null;
    }

    @Override
    public TenantPackageDTO updatePackage(TenantPackageDTO packageDTO) {
        log.error("TenantPackageRpc.updatePackage降级: id={}", packageDTO.getId());
        return null;
    }

    @Override
    public Boolean deletePackage(Long packageId) {
        log.error("TenantPackageRpc.deletePackage降级: packageId={}", packageId);
        return false;
    }

    @Override
    public Boolean togglePackage(Long packageId, Boolean enabled) {
        log.error("TenantPackageRpc.togglePackage降级: packageId={}, enabled={}", packageId, enabled);
        return false;
    }

    @Override
    public TenantPackageDTO copyPackage(Long packageId, String newPackageCode, String newPackageName) {
        log.error("TenantPackageRpc.copyPackage降级: packageId={}, newPackageCode={}, newPackageName={}",
                packageId, newPackageCode, newPackageName);
        return null;
    }

    @Override
    public BigDecimal calculatePrice(Long packageId, Integer durationMonths, String billingCycle) {
        log.error("TenantPackageRpc.calculatePrice降级: packageId={}, durationMonths={}, billingCycle={}",
                packageId, durationMonths, billingCycle);
        return BigDecimal.ZERO;
    }
}
