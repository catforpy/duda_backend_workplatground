package com.duda.user.rpc;

import com.duda.user.dto.company.CompanyServiceProviderDTO;
import com.duda.user.service.company.CompanyServiceProviderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 服务商申请RPC实现类
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "DUDA_USER_GROUP",
    timeout = 30000
)
public class CompanyServiceProviderRpcImpl implements ICompanyServiceProviderRpc {

    @Resource
    private CompanyServiceProviderService companyServiceProviderService;

    @Override
    public CompanyServiceProviderDTO getServiceProviderById(Long id) {
        log.info("【RPC Provider】获取服务商申请，id={}", id);
        return companyServiceProviderService.getServiceProviderById(id);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户服务商列表，tenantId={}", tenantId);
        return companyServiceProviderService.listServiceProvidersByTenantId(tenantId);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByCompany(Long tenantId, Long companyId) {
        log.info("【RPC Provider】根据公司查询服务商列表，tenantId={}, companyId={}", tenantId, companyId);
        return companyServiceProviderService.listServiceProvidersByCompany(tenantId, companyId);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByType(Long tenantId, String applyType) {
        log.info("【RPC Provider】根据类型查询服务商列表，tenantId={}, applyType={}", tenantId, applyType);
        return companyServiceProviderService.listServiceProvidersByType(tenantId, applyType);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByStatus(Long tenantId, String status) {
        log.info("【RPC Provider】根据状态查询服务商列表，tenantId={}, status={}", tenantId, status);
        return companyServiceProviderService.listServiceProvidersByStatus(tenantId, status);
    }

    @Override
    public CompanyServiceProviderDTO createServiceProvider(CompanyServiceProviderDTO serviceProviderDTO) {
        log.info("【RPC Provider】创建服务商申请，applyType={}", serviceProviderDTO.getApplyType());
        return companyServiceProviderService.createServiceProvider(serviceProviderDTO);
    }

    @Override
    public void updateServiceProvider(CompanyServiceProviderDTO serviceProviderDTO) {
        log.info("【RPC Provider】更新服务商申请，id={}", serviceProviderDTO.getId());
        companyServiceProviderService.updateServiceProvider(serviceProviderDTO);
    }

    @Override
    public void deleteServiceProvider(Long id) {
        log.info("【RPC Provider】删除服务商申请，id={}", id);
        companyServiceProviderService.deleteServiceProvider(id);
    }
}
