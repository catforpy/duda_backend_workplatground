package com.duda.user.service.company;

import com.duda.user.dto.company.CompanyServiceProviderDTO;

import java.util.List;

/**
 * 服务商申请服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface CompanyServiceProviderService {

    /**
     * 根据ID查询服务商申请（带缓存）
     *
     * @param id 申请ID
     * @return 服务商申请信息
     */
    CompanyServiceProviderDTO getServiceProviderById(Long id);

    /**
     * 根据租户ID查询服务商列表（带缓存）
     *
     * @param tenantId 租户ID
     * @return 服务商列表
     */
    List<CompanyServiceProviderDTO> listServiceProvidersByTenantId(Long tenantId);

    /**
     * 根据公司ID查询服务商列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param companyId 公司ID
     * @return 服务商列表
     */
    List<CompanyServiceProviderDTO> listServiceProvidersByCompany(Long tenantId, Long companyId);

    /**
     * 根据类型查询服务商列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param applyType 申请类型
     * @return 服务商列表
     */
    List<CompanyServiceProviderDTO> listServiceProvidersByType(Long tenantId, String applyType);

    /**
     * 根据状态查询服务商列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 服务商列表
     */
    List<CompanyServiceProviderDTO> listServiceProvidersByStatus(Long tenantId, String status);

    /**
     * 创建服务商申请
     *
     * @param serviceProviderDTO 服务商申请信息
     * @return 创建的服务商申请信息
     */
    CompanyServiceProviderDTO createServiceProvider(CompanyServiceProviderDTO serviceProviderDTO);

    /**
     * 更新服务商申请
     *
     * @param serviceProviderDTO 服务商申请信息
     */
    void updateServiceProvider(CompanyServiceProviderDTO serviceProviderDTO);

    /**
     * 删除服务商申请
     *
     * @param id 申请ID
     */
    void deleteServiceProvider(Long id);
}
