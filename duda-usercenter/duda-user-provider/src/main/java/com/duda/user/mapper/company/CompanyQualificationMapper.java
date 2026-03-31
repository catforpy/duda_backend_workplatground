package com.duda.user.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.company.CompanyQualification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公司资质文件Mapper接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface CompanyQualificationMapper extends BaseMapper<CompanyQualification> {

    /**
     * 根据租户ID查询资质列表
     */
    List<CompanyQualification> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据公司ID查询资质列表
     */
    List<CompanyQualification> selectByCompanyId(@Param("tenantId") Long tenantId, @Param("companyId") Long companyId);

    /**
     * 根据租户ID和类型查询资质列表
     */
    List<CompanyQualification> selectByTenantIdAndType(@Param("tenantId") Long tenantId, @Param("qualificationType") String qualificationType);

    /**
     * 根据租户ID和审核状态查询资质列表
     */
    List<CompanyQualification> selectByTenantIdAndAuditStatus(@Param("tenantId") Long tenantId, @Param("auditStatus") String auditStatus);
}
