package com.duda.user.service.merchant;

import com.duda.user.dto.merchant.MerchantDTO;
import com.duda.user.entity.merchant.Merchant;

import java.util.List;

/**
 * 商户服务接口
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
public interface MerchantService {

    /**
     * 根据ID查询商户（带缓存）
     *
     * @param merchantId 商户ID
     * @return 商户DTO
     */
    MerchantDTO getMerchantById(Long merchantId);

    /**
     * 根据租户ID和编码查询商户
     *
     * @param tenantId 租户ID
     * @param merchantCode 商户编码
     * @return 商户DTO
     */
    MerchantDTO getMerchantByCode(Long tenantId, String merchantCode);

    /**
     * 创建商户
     *
     * @param merchantDTO 商户DTO
     * @return 创建的商户DTO
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO);

    /**
     * 更新商户
     *
     * @param merchantDTO 商户DTO
     * @return 是否成功
     */
    Boolean updateMerchant(MerchantDTO merchantDTO);

    /**
     * 删除商户（软删除）
     *
     * @param merchantId 商户ID
     * @return 是否成功
     */
    Boolean deleteMerchant(Long merchantId);

    /**
     * 分页查询商户列表
     *
     * @param merchantType 商户类型
     * @param status 状态
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    com.duda.common.domain.PageResult pageMerchants(
            String merchantType, String status, String keyword,
            Integer pageNum, Integer pageSize
    );

    /**
     * 根据平台用户ID查询所有商户（带缓存）
     *
     * @param platformUserId 平台用户ID
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsByPlatformUser(Long platformUserId);

    /**
     * 根据租户ID查询商户列表
     *
     * @param tenantId 租户ID
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsByTenantId(Long tenantId);

    /**
     * 根据租户ID和状态查询商户列表
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsByStatus(Long tenantId, String status);

    /**
     * 分页查询商户列表（根据租户和状态）
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsPage(Long tenantId, String status, Integer pageNum, Integer pageSize);

    /**
     * 更新商户状态和审核状态
     *
     * @param merchantId 商户ID
     * @param status 状态
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     */
    void updateMerchantStatus(Long merchantId, String status, String auditStatus, String auditRemark);

    /**
     * 统计租户下的商户数量
     *
     * @param tenantId 租户ID
     * @return 商户数量
     */
    int countMerchantsByTenantId(Long tenantId);
}
