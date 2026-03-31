package com.duda.user.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.company.CompanyServiceProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务商申请Mapper接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Mapper
public interface CompanyServiceProviderMapper extends BaseMapper<CompanyServiceProvider> {

    /**
     * 根据租户ID查询服务商列表
     */
    List<CompanyServiceProvider> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据公司ID查询服务商列表
     */
    List<CompanyServiceProvider> selectByCompanyId(@Param("tenantId") Long tenantId, @Param("companyId") Long companyId);

    /**
     * 根据租户ID和类型查询服务商列表
     */
    List<CompanyServiceProvider> selectByTenantIdAndType(@Param("tenantId") Long tenantId, @Param("applyType") String applyType);

    /**
     * 根据租户ID和状态查询服务商列表
     */
    List<CompanyServiceProvider> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);
}
