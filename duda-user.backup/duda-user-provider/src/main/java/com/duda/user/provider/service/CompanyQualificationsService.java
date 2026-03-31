package com.duda.user.provider.service;

import com.duda.user.dto.CompanyQualificationsDTO;

import java.util.List;

/**
 * 公司资质文件Service接口
 */
public interface CompanyQualificationsService {

    CompanyQualificationsDTO getQualificationById(Long id);

    List<CompanyQualificationsDTO> listQualificationsByTenantId(Long tenantId);

    List<CompanyQualificationsDTO> listQualificationsByCompany(Long tenantId, Long companyId);

    List<CompanyQualificationsDTO> listQualificationsByType(Long tenantId, String type);

    List<CompanyQualificationsDTO> listQualificationsByAuditStatus(Long tenantId, String status);

    CompanyQualificationsDTO createQualification(CompanyQualificationsDTO dto);

    void updateQualification(CompanyQualificationsDTO dto);

    void deleteQualification(Long id);
}
