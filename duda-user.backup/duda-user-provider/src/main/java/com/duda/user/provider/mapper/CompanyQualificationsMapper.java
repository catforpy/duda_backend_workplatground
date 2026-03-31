package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.CompanyQualificationsPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公司资质文件Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface CompanyQualificationsMapper extends BaseMapper<CompanyQualificationsPO> {

    /**
     * 根据租户ID查询资质列表 ⭐ 新增
     */
    @Select("SELECT * FROM company_qualifications WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<CompanyQualificationsPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据公司ID查询资质列表（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM company_qualifications WHERE tenant_id = #{tenantId} AND company_id = #{companyId} AND deleted = 0 ORDER BY id DESC")
    List<CompanyQualificationsPO> selectByTenantAndCompany(@Param("tenantId") Long tenantId,
                                                          @Param("companyId") Long companyId);

    /**
     * 根据资质类型查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM company_qualifications WHERE tenant_id = #{tenantId} AND qualification_type = #{type} AND deleted = 0 ORDER BY id DESC")
    List<CompanyQualificationsPO> selectByTenantIdAndType(@Param("tenantId") Long tenantId,
                                                         @Param("type") String type);

    /**
     * 根据审核状态查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM company_qualifications WHERE tenant_id = #{tenantId} AND audit_status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<CompanyQualificationsPO> selectByTenantIdAndAuditStatus(@Param("tenantId") Long tenantId,
                                                                 @Param("status") String status);
}
