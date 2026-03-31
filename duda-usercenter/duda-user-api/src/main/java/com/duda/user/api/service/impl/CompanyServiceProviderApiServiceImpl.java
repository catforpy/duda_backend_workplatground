package com.duda.user.api.service.impl;

import com.duda.user.api.service.CompanyServiceProviderApiService;
import com.duda.user.dto.company.CompanyServiceProviderDTO;
import com.duda.user.rpc.ICompanyServiceProviderRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceProviderApiServiceImpl implements CompanyServiceProviderApiService {

    private static final Logger log = LoggerFactory.getLogger(CompanyServiceProviderApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private ICompanyServiceProviderRpc companyServiceProviderRpc;

    @Override
    public CompanyServiceProviderDTO getServiceProviderById(Long id) {
        log.info("【API服务】查询服务商申请，id={}", id);
        return companyServiceProviderRpc.getServiceProviderById(id);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByTenantId(Long tenantId) {
        log.info("【API服务】查询租户服务商列表，tenantId={}", tenantId);
        return companyServiceProviderRpc.listServiceProvidersByTenantId(tenantId);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByCompany(Long tenantId, Long companyId) {
        log.info("【API服务】根据公司查询服务商，tenantId={}, companyId={}", tenantId, companyId);
        return companyServiceProviderRpc.listServiceProvidersByCompany(tenantId, companyId);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByType(Long tenantId, String applyType) {
        log.info("【API服务】根据类型查询服务商，tenantId={}, applyType={}", tenantId, applyType);
        return companyServiceProviderRpc.listServiceProvidersByType(tenantId, applyType);
    }

    @Override
    public List<CompanyServiceProviderDTO> listServiceProvidersByStatus(Long tenantId, String status) {
        log.info("【API服务】根据状态查询服务商，tenantId={}, status={}", tenantId, status);
        return companyServiceProviderRpc.listServiceProvidersByStatus(tenantId, status);
    }

    @Override
    public CompanyServiceProviderDTO createServiceProvider(CompanyServiceProviderDTO serviceProviderDTO) {
        log.info("【API服务】创建服务商申请，companyId={}, applyType={}", serviceProviderDTO.getCompanyId(), serviceProviderDTO.getApplyType());
        return companyServiceProviderRpc.createServiceProvider(serviceProviderDTO);
    }

    @Override
    public void updateServiceProvider(CompanyServiceProviderDTO serviceProviderDTO) {
        log.info("【API服务】更新服务商申请，id={}", serviceProviderDTO.getId());
        companyServiceProviderRpc.updateServiceProvider(serviceProviderDTO);
    }

    @Override
    public void deleteServiceProvider(Long id) {
        log.info("【API服务】删除服务商申请，id={}", id);
        companyServiceProviderRpc.deleteServiceProvider(id);
    }
}
