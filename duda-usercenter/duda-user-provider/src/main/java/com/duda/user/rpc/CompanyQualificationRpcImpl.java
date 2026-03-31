package com.duda.user.rpc;

import com.duda.user.dto.company.CompanyQualificationDTO;
import com.duda.user.service.company.CompanyQualificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 公司资质RPC实现类
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
public class CompanyQualificationRpcImpl implements ICompanyQualificationRpc {

    @Resource
    private CompanyQualificationService companyQualificationService;

    @Override
    public CompanyQualificationDTO getQualificationById(Long id) {
        log.info("【RPC Provider】获取资质信息，id={}", id);
        return companyQualificationService.getQualificationById(id);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByTenantId(Long tenantId) {
        log.info("【RPC Provider】查询租户资质列表，tenantId={}", tenantId);
        return companyQualificationService.listQualificationsByTenantId(tenantId);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByCompany(Long tenantId, Long companyId) {
        log.info("【RPC Provider】根据公司查询资质列表，tenantId={}, companyId={}", tenantId, companyId);
        return companyQualificationService.listQualificationsByCompany(tenantId, companyId);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByType(Long tenantId, String qualificationType) {
        log.info("【RPC Provider】根据类型查询资质列表，tenantId={}, qualificationType={}", tenantId, qualificationType);
        return companyQualificationService.listQualificationsByType(tenantId, qualificationType);
    }

    @Override
    public List<CompanyQualificationDTO> listQualificationsByAuditStatus(Long tenantId, String auditStatus) {
        log.info("【RPC Provider】根据审核状态查询资质列表，tenantId={}, auditStatus={}", tenantId, auditStatus);
        return companyQualificationService.listQualificationsByAuditStatus(tenantId, auditStatus);
    }

    @Override
    public CompanyQualificationDTO createQualification(CompanyQualificationDTO qualificationDTO) {
        log.info("【RPC Provider】创建资质，qualificationType={}", qualificationDTO.getQualificationType());
        return companyQualificationService.createQualification(qualificationDTO);
    }

    @Override
    public void updateQualification(CompanyQualificationDTO qualificationDTO) {
        log.info("【RPC Provider】更新资质，id={}", qualificationDTO.getId());
        companyQualificationService.updateQualification(qualificationDTO);
    }

    @Override
    public void deleteQualification(Long id) {
        log.info("【RPC Provider】删除资质，id={}", id);
        companyQualificationService.deleteQualification(id);
    }
}
