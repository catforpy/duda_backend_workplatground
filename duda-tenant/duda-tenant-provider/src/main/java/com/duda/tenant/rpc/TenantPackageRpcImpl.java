package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.TenantPackageDTO;
import com.duda.tenant.api.rpc.TenantPackageRpc;
import com.duda.tenant.entity.TenantPackage;
import com.duda.tenant.service.TenantPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户套餐RPC实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantPackageRpcImpl implements TenantPackageRpc {

    @Autowired
    private TenantPackageService tenantPackageService;

    @Override
    public TenantPackageDTO getPackageById(Long packageId) {
        log.info("RPC调用: getPackageById, packageId={}", packageId);
        TenantPackage tenantPackage = tenantPackageService.getById(packageId);
        return entityToDto(tenantPackage);
    }

    @Override
    public TenantPackageDTO getPackageByCode(String packageCode) {
        log.info("RPC调用: getPackageByCode, packageCode={}", packageCode);
        TenantPackage tenantPackage = tenantPackageService.getByPackageCode(packageCode);
        return entityToDto(tenantPackage);
    }

    @Override
    public List<TenantPackageDTO> listActivePackages() {
        log.info("RPC调用: listActivePackages");
        List<TenantPackage> packages = tenantPackageService.listActivePackages();
        return packages.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantPackageDTO> listPackagesByType(String packageType) {
        log.info("RPC调用: listPackagesByType, packageType={}", packageType);
        List<TenantPackage> packages = tenantPackageService.listByPackageType(packageType);
        return packages.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantPackageDTO> listByPackageType(String packageType) {
        log.info("RPC调用: listByPackageType, packageType={}", packageType);
        return listPackagesByType(packageType);
    }

    @Override
    public List<TenantPackageDTO> listByTenantId(Long tenantId) {
        log.info("RPC调用: listByTenantId, tenantId={}", tenantId);
        // 根据tenantId查询套餐
        List<TenantPackage> packages = tenantPackageService.lambdaQuery()
                .eq(TenantPackage::getTenantId, tenantId)
                .eq(TenantPackage::getIsActive, 1)
                .orderByAsc(TenantPackage::getSortOrder)
                .list();
        return packages.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantPackageDTO> listByTargetUserType(String targetUserType) {
        log.info("RPC调用: listByTargetUserType, targetUserType={}", targetUserType);
        // 根据targetUserType查询套餐
        List<TenantPackage> packages = tenantPackageService.lambdaQuery()
                .eq(TenantPackage::getTargetUserType, targetUserType)
                .eq(TenantPackage::getIsActive, 1)
                .orderByAsc(TenantPackage::getSortOrder)
                .list();
        return packages.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TenantPackageDTO createPackage(TenantPackageDTO packageDTO) {
        log.info("RPC调用: createPackage, packageCode={}", packageDTO.getPackageCode());
        TenantPackage entity = dtoToEntity(packageDTO);
        TenantPackage created = tenantPackageService.createPackage(entity);
        return entityToDto(created);
    }

    @Override
    public TenantPackageDTO updatePackage(TenantPackageDTO packageDTO) {
        log.info("RPC调用: updatePackage, id={}", packageDTO.getId());
        TenantPackage entity = dtoToEntity(packageDTO);
        TenantPackage updated = tenantPackageService.updatePackage(entity);
        return entityToDto(updated);
    }

    @Override
    public Boolean deletePackage(Long packageId) {
        log.info("RPC调用: deletePackage, packageId={}", packageId);
        return tenantPackageService.deletePackage(packageId);
    }

    @Override
    public Boolean togglePackage(Long packageId, Boolean enabled) {
        log.info("RPC调用: togglePackage, packageId={}, enabled={}", packageId, enabled);
        return tenantPackageService.togglePackage(packageId, enabled);
    }

    @Override
    public TenantPackageDTO copyPackage(Long packageId, String newPackageCode, String newPackageName) {
        log.info("RPC调用: copyPackage, packageId={}, newPackageCode={}, newPackageName={}",
                packageId, newPackageCode, newPackageName);

        // 查询原套餐
        TenantPackage original = tenantPackageService.getById(packageId);
        if (original == null) {
            return null;
        }

        // 复制套餐
        TenantPackage copy = new TenantPackage();
        copy.setPackageCode(newPackageCode);
        copy.setPackageName(newPackageName);
        copy.setPackageType(original.getPackageType());
        copy.setTenantId(original.getTenantId());
        copy.setTargetUserType(original.getTargetUserType());
        copy.setMaxUsers(original.getMaxUsers());
        copy.setMaxStorageSize(original.getMaxStorageSize());
        copy.setMaxApiCallsPerDay(original.getMaxApiCallsPerDay());
        copy.setPriceMonthly(original.getPriceMonthly());
        copy.setPriceYearly(original.getPriceYearly());
        copy.setFeatures(original.getFeatures());
        copy.setSortOrder(original.getSortOrder());
        copy.setIsActive(1); // 新套餐默认启用

        TenantPackage created = tenantPackageService.createPackage(copy);
        return entityToDto(created);
    }

    @Override
    public BigDecimal calculatePrice(Long packageId, Integer durationMonths, String billingCycle) {
        log.info("RPC调用: calculatePrice, packageId={}, durationMonths={}, billingCycle={}",
                packageId, durationMonths, billingCycle);
        return tenantPackageService.calculatePrice(packageId, durationMonths, billingCycle);
    }

    /**
     * Entity转DTO
     */
    private TenantPackageDTO entityToDto(TenantPackage entity) {
        if (entity == null) {
            return null;
        }
        TenantPackageDTO dto = new TenantPackageDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO转Entity
     */
    private TenantPackage dtoToEntity(TenantPackageDTO dto) {
        if (dto == null) {
            return null;
        }
        TenantPackage entity = new TenantPackage();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
