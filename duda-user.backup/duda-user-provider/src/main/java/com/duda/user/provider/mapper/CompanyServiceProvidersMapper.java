package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.CompanyServiceProvidersPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务商申请Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface CompanyServiceProvidersMapper extends BaseMapper<CompanyServiceProvidersPO> {

    /**
     * 根据租户ID查询服务商申请列表 ⭐ 新增
     */
    @Select("SELECT * FROM company_service_providers WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<CompanyServiceProvidersPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据公司ID查询服务商申请列表（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM company_service_providers WHERE tenant_id = #{tenantId} AND company_id = #{companyId} AND deleted = 0 ORDER BY id DESC")
    List<CompanyServiceProvidersPO> selectByTenantAndCompany(@Param("tenantId") Long tenantId,
                                                           @Param("companyId") Long companyId);

    /**
     * 根据申请类型查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM company_service_providers WHERE tenant_id = #{tenantId} AND apply_type = #{type} AND deleted = 0 ORDER BY id DESC")
    List<CompanyServiceProvidersPO> selectByTenantIdAndType(@Param("tenantId") Long tenantId,
                                                          @Param("type") String type);

    /**
     * 根据状态查询（带租户隔离）⭐ 新增
     */
    @Select("SELECT * FROM company_service_providers WHERE tenant_id = #{tenantId} AND status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<CompanyServiceProvidersPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                                             @Param("status") String status);
}
