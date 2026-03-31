package com.duda.user.api.service;

import com.duda.user.dto.company.CompanyQualificationDTO;

import java.util.List;

/**
 * 公司资质API服务接口
 */
public interface CompanyQualificationApiService {

    CompanyQualificationDTO getQualificationById(Long id);

    List<CompanyQualificationDTO> listQualificationsByTenantId(Long tenantId);

    List<CompanyQualificationDTO> listQualificationsByCompany(Long tenantId, Long companyId);

    List<CompanyQualificationDTO> listQualificationsByType(Long tenantId, String qualificationType);

    List<CompanyQualificationDTO> listQualificationsByAuditStatus(Long tenantId, String auditStatus);

    CompanyQualificationDTO createQualification(CompanyQualificationDTO qualificationDTO);

    void updateQualification(CompanyQualificationDTO qualificationDTO);

    void deleteQualification(Long id);
}
