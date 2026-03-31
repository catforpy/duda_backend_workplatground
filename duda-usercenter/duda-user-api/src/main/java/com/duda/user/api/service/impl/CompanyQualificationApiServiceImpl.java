package com.duda.user.api.service.impl;

import com.duda.user.api.service.CompanyQualificationApiService;
import com.duda.user.dto.company.CompanyQualificationDTO;
import com.duda.user.rpc.ICompanyQualificationRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyQualificationApiServiceImpl implements CompanyQualificationApiService {

    private static final Logger log = LoggerFactory.getLogger(CompanyQualificationApiServiceImpl.class);

    @DubboReference(version = "1.0.0", group = "DUDA_USER_GROUP", timeout = 30000, check = false)
    private ICompanyQualificationRpc companyQualificationRpc;

    @Override
    public CompanyQualificationDTO getQualificationById(Long id) {
        log.info("【API服务】查询资质信息，id={}", id);
        return companyQualificationRpc.getQualificationById(id);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByTenantId(Long tenantId) {
        log.info("【API服务】查询租户资质列表，tenantId={}", tenantId);
        return companyQualificationRpc.listQualificationsByTenantId(tenantId);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByCompany(Long tenantId, Long companyId) {
        log.info("【API服务】根据公司查询资质，tenantId={}, companyId={}", tenantId, companyId);
        return companyQualificationRpc.listQualificationsByCompany(tenantId, companyId);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByType(Long tenantId, String qualificationType) {
        log.info("【API服务】根据类型查询资质，tenantId={}, type={}", tenantId, qualificationType);
        return companyQualificationRpc.listQualificationsByType(tenantId, qualificationType);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByAuditStatus(Long tenantId, String auditStatus) {
        log.info("【API服务】根据审核状态查询资质，tenantId={}, auditStatus={}", tenantId, auditStatus);
        return companyQualificationRpc.listQualificationsByAuditStatus(tenantId, auditStatus);
    }

    @Override
    public CompanyQualificationDTO createQualification(CompanyQualificationDTO qualificationDTO) {
        log.info("【API服务】创建资质信息，companyId={}, type={}", qualificationDTO.getCompanyId(), qualificationDTO.getQualificationType());
        return companyQualificationRpc.createQualification(qualificationDTO);
    }

    @Override
    public void updateQualification(CompanyQualificationDTO qualificationDTO) {
        log.info("【API服务】更新资质信息，id={}", qualificationDTO.getId());
        companyQualificationRpc.updateQualification(qualificationDTO);
    }

    @Override
    public void deleteQualification(Long id) {
        log.info("【API服务】删除资质信息，id={}", id);
        companyQualificationRpc.deleteQualification(id);
    }
}
