package com.duda.user.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.merchant.MerchantUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商户用户映射Mapper
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Mapper
public interface MerchantUserMapper extends BaseMapper<MerchantUser> {

    /**
     * 根据商户ID查询用户列表
     *
     * @param merchantId 商户ID
     * @return 用户列表
     */
    @Select("SELECT * FROM merchant_users WHERE merchant_id = #{merchantId} AND deleted = 0")
    List<MerchantUser> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据平台用户ID查询所有商户信息
     *
     * @param platformUserId 平台用户ID
     * @return 用户-商户映射列表
     */
    @Select("SELECT * FROM merchant_users WHERE platform_user_id = #{platformUserId} AND deleted = 0")
    List<MerchantUser> selectByPlatformUserId(@Param("platformUserId") Long platformUserId);

    /**
     * 根据商户ID和平台用户ID查询
     *
     * @param merchantId 商户ID
     * @param platformUserId 平台用户ID
     * @return 用户-商户映射
     */
    @Select("SELECT * FROM merchant_users WHERE merchant_id = #{merchantId} AND platform_user_id = #{platformUserId} AND deleted = 0 LIMIT 1")
    MerchantUser selectByMerchantIdAndPlatformUserId(@Param("merchantId") Long merchantId, @Param("platformUserId") Long platformUserId);

    /**
     * 根据OpenID查询
     *
     * @param merchantId 商户ID
     * @param openid OpenID
     * @return 用户-商户映射
     */
    @Select("SELECT * FROM merchant_users WHERE merchant_id = #{merchantId} AND mini_app_openid = #{openid} AND deleted = 0 LIMIT 1")
    MerchantUser selectByMerchantIdAndOpenid(@Param("merchantId") Long merchantId, @Param("openid") String openid);
}
