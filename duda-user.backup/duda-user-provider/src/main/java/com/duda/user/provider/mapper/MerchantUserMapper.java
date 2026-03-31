package com.duda.user.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.provider.po.MerchantUserPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商户用户Mapper接口
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MerchantUserMapper extends BaseMapper<MerchantUserPO> {

    /**
     * 根据租户ID查询商户用户列表 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return 商户用户列表
     */
    @Select("SELECT * FROM merchant_users WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY id DESC")
    List<MerchantUserPO> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据商户ID查询用户列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @return 商户用户列表
     */
    @Select("SELECT * FROM merchant_users WHERE tenant_id = #{tenantId} AND merchant_id = #{merchantId} AND deleted = 0 ORDER BY id DESC")
    List<MerchantUserPO> selectByTenantAndMerchant(@Param("tenantId") Long tenantId,
                                                   @Param("merchantId") Long merchantId);

    /**
     * 根据租户ID和商户用户ID查询 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param merchantUserId 商户用户ID
     * @return 商户用户信息
     */
    @Select("SELECT * FROM merchant_users WHERE tenant_id = #{tenantId} AND merchant_id = #{merchantId} AND merchant_user_id = #{merchantUserId} AND deleted = 0")
    MerchantUserPO selectByTenantAndMerchantUserId(@Param("tenantId") Long tenantId,
                                                    @Param("merchantId") Long merchantId,
                                                    @Param("merchantUserId") String merchantUserId);

    /**
     * 根据租户ID和小程序OpenID查询 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param openid 小程序OpenID
     * @return 商户用户信息
     */
    @Select("SELECT * FROM merchant_users WHERE tenant_id = #{tenantId} AND merchant_id = #{merchantId} AND mini_app_openid = #{openid} AND deleted = 0")
    MerchantUserPO selectByTenantAndOpenid(@Param("tenantId") Long tenantId,
                                           @Param("merchantId") Long merchantId,
                                           @Param("openid") String openid);

    /**
     * 统计租户下的商户用户数量 ⭐ 新增
     *
     * @param tenantId 租户ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM merchant_users WHERE tenant_id = #{tenantId} AND deleted = 0")
    int countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 统计商户用户数量（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM merchant_users WHERE tenant_id = #{tenantId} AND merchant_id = #{merchantId} AND deleted = 0")
    int countByTenantAndMerchant(@Param("tenantId") Long tenantId,
                                  @Param("merchantId") Long merchantId);

    /**
     * 根据平台用户ID查询所有商户关联（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param platformUserId 平台用户ID
     * @param platformUserShard 平台用户分片
     * @return 商户用户列表
     */
    List<MerchantUserPO> selectByPlatformUser(@Param("tenantId") Long tenantId,
                                              @Param("platformUserId") Long platformUserId,
                                              @Param("platformUserShard") Byte platformUserShard);

    /**
     * 分页查询商户用户列表（带租户隔离）⭐ 新增
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID（可选）
     * @param status 状态（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商户用户列表
     */
    List<MerchantUserPO> selectPageWithTenant(@Param("tenantId") Long tenantId,
                                             @Param("merchantId") Long merchantId,
                                             @Param("status") Byte status,
                                             @Param("offset") Long offset,
                                             @Param("limit") Integer limit);
}
