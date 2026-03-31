package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.MerchantPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商户Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MerchantMapper extends BaseMapper<MerchantPO> {

    /**
     * 根据租户ID查询商户列表 ⭐ 新增（支持租户隔离）
     *
     * @param tenantId 租户ID
     * @return 商户列表
     */
    @Select("SELECT * FROM merchants WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<MerchantPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID和商户编码查询 ⭐ 新增（支持租户隔离）
     *
     * @param tenantId 租户ID
     * @param merchantCode 商户编码
     * @return 商户信息
     */
    @Select("SELECT * FROM merchants WHERE tenant_id = #{tenantId} AND merchant_code = #{merchantCode} AND deleted = 0")
    MerchantPO selectByTenantAndCode(@Param("tenantId") Long tenantId,
                                     @Param("merchantCode") String merchantCode);

    /**
     * 统计租户下的商户数量 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return 商户数量
     */
    @Select("SELECT COUNT(*) FROM merchants WHERE tenant_id = #{tenantId} AND deleted = 0")
    int countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID和状态查询商户列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 商户列表
     */
    @Select("SELECT * FROM merchants WHERE tenant_id = #{tenantId} AND status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<MerchantPO> selectByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                               @Param("status") String status);

    /**
     * 分页查询商户列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param status 状态（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商户列表
     */
    List<MerchantPO> selectPageWithTenant(@Param("tenantId") Long tenantId,
                                         @Param("status") String status,
                                         @Param("offset") Long offset,
                                         @Param("limit") Integer limit);

    /**
     * 根据公司ID查询商户列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param companyId 公司ID
     * @return 商户列表
     */
    @Select("SELECT * FROM merchants WHERE tenant_id = #{tenantId} AND company_id = #{companyId} AND deleted = 0 ORDER BY id DESC")
    List<MerchantPO> selectByTenantAndCompany(@Param("tenantId") Long tenantId,
                                             @Param("companyId") Long companyId);

    /**
     * 更新商户状态（带乐观锁）⭐ 新增
     *
     * @param merchant 商户信息
     * @return 影响行数
     */
    int updateStatusWithVersion(MerchantPO merchant);
}
