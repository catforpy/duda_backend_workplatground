package com.duda.tenant.api.service.impl;

import com.duda.tenant.api.dto.TenantPackageDTO;
import com.duda.tenant.api.rpc.TenantPackageRpc;
import com.duda.tenant.api.service.TenantPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户套餐Service实现类
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Slf4j
@Service
public class TenantPackageServiceImpl implements TenantPackageService {

    @DubboReference(group = "DUDA_TENANT_GROUP", version = "1.0.0")
    private TenantPackageRpc tenantPackageRpc;

    @Override
    public List<TenantPackageDTO> listAll() {
        log.debug("查询所有套餐");
        return tenantPackageRpc.listActivePackages();
    }

    @Override
    public TenantPackageDTO getById(Long id) {
        log.debug("查询套餐: id={}", id);
        return tenantPackageRpc.getPackageById(id);
    }

    @Override
    public TenantPackageDTO create(TenantPackageDTO packageDTO) {
        log.info("创建套餐: packageCode={}, packageName={}", packageDTO.getPackageCode(), packageDTO.getPackageName());
        return tenantPackageRpc.createPackage(packageDTO);
    }

    @Override
    public TenantPackageDTO update(TenantPackageDTO packageDTO) {
        log.info("更新套餐: id={}, packageCode={}", packageDTO.getId(), packageDTO.getPackageCode());
        return tenantPackageRpc.updatePackage(packageDTO);
    }

    @Override
    public Boolean delete(Long id) {
        log.info("删除套餐: id={}", id);
        return tenantPackageRpc.deletePackage(id);
    }

    @Override
    public Boolean toggleActive(Long id, Boolean enabled) {
        log.info("{}套餐: id={}", enabled ? "启用" : "禁用", id);
        return tenantPackageRpc.togglePackage(id, enabled);
    }

    @Override
    public List<TenantPackageDTO> listByPackageType(String packageType) {
        log.debug("根据套餐类型查询: packageType={}", packageType);
        return tenantPackageRpc.listByPackageType(packageType);
    }

    @Override
    public List<TenantPackageDTO> listByTenantId(Long tenantId) {
        log.debug("根据租户ID查询套餐: tenantId={}", tenantId);
        return tenantPackageRpc.listByTenantId(tenantId);
    }

    @Override
    public List<TenantPackageDTO> listByTargetUserType(String targetUserType) {
        log.debug("根据目标用户类型查询: targetUserType={}", targetUserType);
        return tenantPackageRpc.listByTargetUserType(targetUserType);
    }

    @Override
    public TenantPackageDTO copy(Long id, String newPackageCode, String newPackageName) {
        log.info("复制套餐: id={}, newPackageCode={}, newPackageName={}", id, newPackageCode, newPackageName);
        return tenantPackageRpc.copyPackage(id, newPackageCode, newPackageName);
    }
}
