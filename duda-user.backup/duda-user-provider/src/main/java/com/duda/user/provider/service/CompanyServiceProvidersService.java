package com.duda.user.provider.service;

import com.duda.user.dto.CompanyServiceProvidersDTO;

import java.util.List;

/**
 * 服务商申请Service接口
 */
public interface CompanyServiceProvidersService {

    CompanyServiceProvidersDTO getServiceProviderById(Long id);

    List<CompanyServiceProvidersDTO> listServiceProvidersByTenantId(Long tenantId);

    List<CompanyServiceProvidersDTO> listServiceProvidersByCompany(Long tenantId, Long companyId);

    List<CompanyServiceProvidersDTO> listServiceProvidersByType(Long tenantId, String type);

    List<CompanyServiceProvidersDTO> listServiceProvidersByStatus(Long tenantId, String status);

    CompanyServiceProvidersDTO createServiceProvider(CompanyServiceProvidersDTO dto);

    void updateServiceProvider(CompanyServiceProvidersDTO dto);

    void deleteServiceProvider(Long id);
}
