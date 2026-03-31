package com.duda.user.rpc;

import com.duda.user.dto.merchant.MerchantUserDTO;

import java.util.List;

/**
 * 商户用户RPC接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface IMerchantUserRpc {

    /**
     * 根据ID查询商户用户
     *
     * @param id 商户用户ID
     * @return 商户用户信息
     */
    MerchantUserDTO getMerchantUserById(Long id);

    /**
     * 根据租户ID查询商户用户列表
     *
     * @param tenantId 租户ID
     * @return 商户用户列表
     */
    List<MerchantUserDTO> listMerchantUsersByTenantId(Long tenantId);

    /**
     * 根据商户ID查询商户用户列表
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @return 商户用户列表
     */
    List<MerchantUserDTO> listMerchantUsersByMerchant(Long tenantId, Long merchantId);

    /**
     * 根据用户ID查询商户用户
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param merchantUserId 商户用户ID
     * @return 商户用户信息
     */
    MerchantUserDTO getMerchantUserByUserId(Long tenantId, Long merchantId, String merchantUserId);

    /**
     * 根据OpenID查询商户用户
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param openid 微信OpenID
     * @return 商户用户信息
     */
    MerchantUserDTO getMerchantUserByOpenid(Long tenantId, Long merchantId, String openid);

    /**
     * 根据平台用户ID查询商户列表
     *
     * @param tenantId 租户ID
     * @param platformUserId 平台用户ID
     * @param platformUserShard 平台用户分片
     * @return 商户用户列表
     */
    List<MerchantUserDTO> listMerchantUsersByPlatformUser(Long tenantId, Long platformUserId, Byte platformUserShard);

    /**
     * 分页查询商户用户列表
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商户用户列表
     */
    List<MerchantUserDTO> listMerchantUsersPage(Long tenantId, Long merchantId, Byte status,
                                                Integer pageNum, Integer pageSize);

    /**
     * 创建商户用户
     *
     * @param merchantUserDTO 商户用户信息
     * @return 创建的商户用户信息
     */
    MerchantUserDTO createMerchantUser(MerchantUserDTO merchantUserDTO);

    /**
     * 更新商户用户
     *
     * @param merchantUserDTO 商户用户信息
     */
    void updateMerchantUser(MerchantUserDTO merchantUserDTO);

    /**
     * 删除商户用户
     *
     * @param id 商户用户ID
     */
    void deleteMerchantUser(Long id);

    /**
     * 统计租户下的商户用户数量
     *
     * @param tenantId 租户ID
     * @return 商户用户数量
     */
    int countMerchantUsersByTenantId(Long tenantId);

    /**
     * 统计商户下的用户数量
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @return 用户数量
     */
    int countMerchantUsersByMerchant(Long tenantId, Long merchantId);

    /**
     * 统计用户关联的商户数量
     *
     * @param platformUserId 平台用户ID
     * @return 商户数量
     */
    int countMerchantsByPlatformUserId(Long platformUserId);
}
