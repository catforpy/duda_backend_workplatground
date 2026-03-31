package com.duda.user.api.service;

import com.duda.user.dto.company.CompanyServiceProviderDTO;

import java.util.List;

/**
 * 服务商申请API服务接口
 */
public interface CompanyServiceProviderApiService {

    CompanyServiceProviderDTO getServiceProviderById(Long id);

    List<CompanyServiceProviderDTO> listServiceProvidersByTenantId(Long tenantId);

    List<CompanyServiceProviderDTO> listServiceProvidersByCompany(Long tenantId, Long companyId);

    List<CompanyServiceProviderDTO> listServiceProvidersByType(Long tenantId, String applyType);

    List<CompanyServiceProviderDTO> listServiceProvidersByStatus(Long tenantId, String status);

    CompanyServiceProviderDTO createServiceProvider(CompanyServiceProviderDTO serviceProviderDTO);

    void updateServiceProvider(CompanyServiceProviderDTO serviceProviderDTO);

    void deleteServiceProvider(Long id);
}
