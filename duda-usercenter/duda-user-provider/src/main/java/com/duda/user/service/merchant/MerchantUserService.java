package com.duda.user.service.merchant;

import com.duda.common.domain.PageResult;
import com.duda.user.dto.merchant.MerchantUserDTO;

import java.util.List;

/**
 * 商户用户映射服务接口
 *
 * 说明：多租户SAAS模式的核心服务
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
public interface MerchantUserService {

    /**
     * 根据商户ID查询用户列表（带缓存）
     *
     * @param merchantId 商户ID
     * @return 用户-商户映射列表
     */
    List<MerchantUserDTO> listUsersByMerchantId(Long merchantId);

    /**
     * 根据平台用户ID查询所有商户（带缓存）
     *
     * @param platformUserId 平台用户ID
     * @return 用户-商户映射列表
     */
    List<MerchantUserDTO> listMerchantsByPlatformUserId(Long platformUserId);

    /**
     * 根据商户ID和平台用户ID查询映射关系（带缓存）
     *
     * @param merchantId 商户ID
     * @param platformUserId 平台用户ID
     * @return 用户-商户映射
     */
    MerchantUserDTO getMerchantUser(Long merchantId, Long platformUserId);

    /**
     * 根据OpenID查询用户（带缓存）
     *
     * @param merchantId 商户ID
     * @param openid 微信OpenID
     * @return 用户-商户映射
     */
    MerchantUserDTO getMerchantUserByOpenid(Long merchantId, String openid);

    /**
     * 创建或更新用户-商户映射关系
     *
     * @param merchantUserDTO 用户-商户映射DTO
     * @return 是否成功
     */
    Boolean bindMerchantUser(MerchantUserDTO merchantUserDTO);

    /**
     * 更新用户访问信息
     *
     * @param merchantId 商户ID
     * @param platformUserId 平台用户ID
     * @return 是否成功
     */
    Boolean updateVisitInfo(Long merchantId, Long platformUserId);

    /**
     * 解绑用户-商户关系
     *
     * @param merchantId 商户ID
     * @param platformUserId 平台用户ID
     * @return 是否成功
     */
    Boolean unbindMerchantUser(Long merchantId, Long platformUserId);

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
     * 根据租户ID和商户ID查询商户用户列表
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @return 商户用户列表
     */
    List<MerchantUserDTO> listMerchantUsersByMerchant(Long tenantId, Long merchantId);

    /**
     * 根据租户ID、商户ID和商户用户ID查询
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param merchantUserId 商户用户ID
     * @return 商户用户信息
     */
    MerchantUserDTO getMerchantUserByUserId(Long tenantId, Long merchantId, String merchantUserId);

    /**
     * 根据租户ID、商户ID和OpenID查询商户用户
     *
     * @param tenantId 租户ID
     * @param merchantId 商户ID
     * @param openid 微信OpenID
     * @return 商户用户信息
     */
    MerchantUserDTO getMerchantUserByOpenid(Long tenantId, Long merchantId, String openid);

    /**
     * 根据租户ID、平台用户ID查询商户列表
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
