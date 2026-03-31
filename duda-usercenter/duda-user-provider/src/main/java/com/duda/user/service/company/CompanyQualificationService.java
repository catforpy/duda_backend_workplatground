package com.duda.user.service.company;

import com.duda.user.dto.company.CompanyQualificationDTO;

import java.util.List;

/**
 * 公司资质服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface CompanyQualificationService {

    /**
     * 根据ID查询资质信息（带缓存）
     *
     * @param id 资质ID
     * @return 资质信息
     */
    CompanyQualificationDTO getQualificationById(Long id);

    /**
     * 根据租户ID查询资质列表（带缓存）
     *
     * @param tenantId 租户ID
     * @return 资质列表
     */
    List<CompanyQualificationDTO> listQualificationsByTenantId(Long tenantId);

    /**
     * 根据公司ID查询资质列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param companyId 公司ID
     * @return 资质列表
     */
    List<CompanyQualificationDTO> listQualificationsByCompany(Long tenantId, Long companyId);

    /**
     * 根据类型查询资质列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param qualificationType 资质类型
     * @return 资质列表
     */
    List<CompanyQualificationDTO> listQualificationsByType(Long tenantId, String qualificationType);

    /**
     * 根据审核状态查询资质列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param auditStatus 审核状态
     * @return 资质列表
     */
    List<CompanyQualificationDTO> listQualificationsByAuditStatus(Long tenantId, String auditStatus);

    /**
     * 创建资质信息
     *
     * @param qualificationDTO 资质信息
     * @return 创建的资质信息
     */
    CompanyQualificationDTO createQualification(CompanyQualificationDTO qualificationDTO);

    /**
     * 更新资质信息
     *
     * @param qualificationDTO 资质信息
     */
    void updateQualification(CompanyQualificationDTO qualificationDTO);

    /**
     * 删除资质信息
     *
     * @param id 资质ID
     */
    void deleteQualification(Long id);
}
