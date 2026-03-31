package com.duda.tenant.rpc;

import com.duda.tenant.api.dto.*;
import com.duda.tenant.api.rpc.TenantRpc;
import com.duda.tenant.entity.Tenant;
import com.duda.tenant.service.TenantService;
import com.duda.tenant.service.TenantStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 租户RPC实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@DubboService(group = "DUDA_TENANT_GROUP", version = "1.0.0", timeout = 5000)
public class TenantRpcImpl implements TenantRpc {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantStatisticsService tenantStatisticsService;

    @Override
    public TenantDTO getTenantById(Long tenantId) {
        log.info("RPC调用: getTenantById, tenantId={}", tenantId);
        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            return null;
        }
        return entityToDto(tenant);
    }

    @Override
    public TenantDTO getTenantByCode(String tenantCode) {
        log.info("RPC调用: getTenantByCode, tenantCode={}", tenantCode);
        Tenant tenant = tenantService.getByTenantCode(tenantCode);
        if (tenant == null) {
            return null;
        }
        return entityToDto(tenant);
    }

    @Override
    public TenantDTO createTenant(TenantDTO tenantDTO) {
        log.info("RPC调用: createTenant, tenantCode={}, tenantName={}",
                tenantDTO.getTenantCode(), tenantDTO.getTenantName());
        Tenant tenant = dtoToEntity(tenantDTO);
        tenant = tenantService.createTenant(tenant);
        return entityToDto(tenant);
    }

    @Override
    public TenantDTO updateTenant(TenantDTO tenantDTO) {
        log.info("RPC调用: updateTenant, id={}", tenantDTO.getId());
        Tenant tenant = dtoToEntity(tenantDTO);
        tenant = tenantService.updateTenant(tenant);
        return entityToDto(tenant);
    }

    @Override
    public TenantCheckDTO checkTenantValid(Long tenantId) {
        log.info("RPC调用: checkTenantValid, tenantId={}", tenantId);
        TenantCheckDTO checkDTO = new TenantCheckDTO();
        checkDTO.setTenantId(tenantId);

        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            checkDTO.setIsValid(false);
            checkDTO.setErrorMessage("租户不存在");
            return checkDTO;
        }

        checkDTO.setTenantCode(tenant.getTenantCode());
        checkDTO.setTenantName(tenant.getTenantName());
        checkDTO.setTenantStatus(tenant.getTenantStatus());

        // 检查是否过期
        boolean isExpired = tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now());
        checkDTO.setIsExpired(isExpired);

        // 检查是否暂停
        boolean isSuspended = "suspended".equals(tenant.getTenantStatus());
        checkDTO.setIsSuspended(isSuspended);

        // 检查是否有效
        boolean isValid = tenantService.isValidTenant(tenantId);
        checkDTO.setIsValid(isValid);

        if (!isValid) {
            if (isExpired) {
                checkDTO.setErrorMessage("租户已过期");
            } else if (isSuspended) {
                checkDTO.setErrorMessage("租户已暂停");
            } else {
                checkDTO.setErrorMessage("租户无效");
            }
        }

        return checkDTO;
    }

    @Override
    public QuotaCheckDTO checkQuota(Long tenantId, String quotaType) {
        log.info("RPC调用: checkQuota, tenantId={}, quotaType={}", tenantId, quotaType);
        QuotaCheckDTO checkDTO = new QuotaCheckDTO();
        checkDTO.setQuotaType(quotaType);

        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            checkDTO.setPassed(false);
            checkDTO.setErrorMessage("租户不存在");
            return checkDTO;
        }

        // 获取最大值
        Long maxValue = null;
        switch (quotaType) {
            case "user_count":
                maxValue = tenant.getMaxUsers() != null ? tenant.getMaxUsers() * 1L : null;
                break;
            case "storage_size":
                maxValue = tenant.getMaxStorageSize();
                break;
            case "api_calls":
                maxValue = tenant.getMaxApiCallsPerDay() != null ? tenant.getMaxApiCallsPerDay() * 1L : null;
                break;
            default:
                checkDTO.setPassed(false);
                checkDTO.setErrorMessage("未知的配额类型");
                return checkDTO;
        }

        checkDTO.setMaxValue(maxValue);

        // 检查配额
        Boolean passed = tenantService.checkQuota(tenantId, quotaType);
        checkDTO.setPassed(passed != null && passed);

        if (checkDTO.getPassed()) {
            checkDTO.setErrorMessage(null);
        } else {
            checkDTO.setErrorMessage("配额已超限");
        }

        return checkDTO;
    }

    @Override
    public Boolean updatePackage(Long tenantId, Long packageId) {
        log.info("RPC调用: updatePackage, tenantId={}, packageId={}", tenantId, packageId);
        return tenantService.updatePackage(tenantId, packageId);
    }

    @Override
    public Boolean suspendTenant(Long tenantId) {
        log.info("RPC调用: suspendTenant, tenantId={}", tenantId);
        return tenantService.suspendTenant(tenantId);
    }

    @Override
    public Boolean activateTenant(Long tenantId) {
        log.info("RPC调用: activateTenant, tenantId={}", tenantId);
        return tenantService.activateTenant(tenantId);
    }

    @Override
    public Map<String, String> getTenantConfigMap(Long tenantId) {
        log.info("RPC调用: getTenantConfigMap, tenantId={}", tenantId);
        // TODO: 调用TenantConfigService
        return null;
    }

    @Override
    public Boolean recordApiCall(Long tenantId) {
        log.info("RPC调用: recordApiCall, tenantId={}", tenantId);
        return tenantStatisticsService.checkAndUpdateApiCalls(tenantId);
    }

    /**
     * Entity转DTO
     */
    private TenantDTO entityToDto(Tenant entity) {
        if (entity == null) {
            return null;
        }
        TenantDTO dto = new TenantDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO转Entity
     */
    private Tenant dtoToEntity(TenantDTO dto) {
        if (dto == null) {
            return null;
        }
        Tenant entity = new Tenant();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
