package com.duda.tenant.api.fallback;

import com.duda.tenant.api.dto.*;
import com.duda.tenant.api.rpc.TenantRpc;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 租户RPC降级实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
public class TenantRpcFallback implements TenantRpc {

    @Override
    public TenantDTO getTenantById(Long tenantId) {
        log.error("TenantRpc.getTenantById降级: tenantId={}", tenantId);
        return null;
    }

    @Override
    public TenantDTO getTenantByCode(String tenantCode) {
        log.error("TenantRpc.getTenantByCode降级: tenantCode={}", tenantCode);
        return null;
    }

    @Override
    public TenantDTO createTenant(TenantDTO tenantDTO) {
        log.error("TenantRpc.createTenant降级: tenantCode={}", tenantDTO.getTenantCode());
        return null;
    }

    @Override
    public TenantDTO updateTenant(TenantDTO tenantDTO) {
        log.error("TenantRpc.updateTenant降级: id={}", tenantDTO.getId());
        return null;
    }

    @Override
    public TenantCheckDTO checkTenantValid(Long tenantId) {
        log.error("TenantRpc.checkTenantValid降级: tenantId={}", tenantId);
        TenantCheckDTO checkDTO = new TenantCheckDTO();
        checkDTO.setIsValid(false);
        checkDTO.setErrorMessage("租户服务不可用");
        return checkDTO;
    }

    @Override
    public QuotaCheckDTO checkQuota(Long tenantId, String quotaType) {
        log.error("TenantRpc.checkQuota降级: tenantId={}, quotaType={}", tenantId, quotaType);
        QuotaCheckDTO checkDTO = new QuotaCheckDTO();
        checkDTO.setPassed(false);
        checkDTO.setQuotaType(quotaType);
        checkDTO.setErrorMessage("租户服务不可用");
        return checkDTO;
    }

    @Override
    public Boolean updatePackage(Long tenantId, Long packageId) {
        log.error("TenantRpc.updatePackage降级: tenantId={}, packageId={}", tenantId, packageId);
        return false;
    }

    @Override
    public Boolean suspendTenant(Long tenantId) {
        log.error("TenantRpc.suspendTenant降级: tenantId={}", tenantId);
        return false;
    }

    @Override
    public Boolean activateTenant(Long tenantId) {
        log.error("TenantRpc.activateTenant降级: tenantId={}", tenantId);
        return false;
    }

    @Override
    public Map<String, String> getTenantConfigMap(Long tenantId) {
        log.error("TenantRpc.getTenantConfigMap降级: tenantId={}", tenantId);
        return new HashMap<>();
    }

    @Override
    public Boolean recordApiCall(Long tenantId) {
        log.error("TenantRpc.recordApiCall降级: tenantId={}", tenantId);
        return true; // 降级时默认允许通过，避免影响业务
    }
}
