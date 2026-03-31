package com.duda.user.api.service;

import com.duda.user.dto.merchant.MerchantDTO;

import java.util.List;

/**
 * 商户API服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MerchantApiService {

    /**
     * 根据ID查询商户
     *
     * @param merchantId 商户ID
     * @return 商户信息
     */
    MerchantDTO getMerchantById(Long merchantId);

    /**
     * 根据租户ID查询商户列表
     *
     * @param tenantId 租户ID
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsByTenantId(Long tenantId);

    /**
     * 根据商户编码查询
     *
     * @param tenantId 租户ID
     * @param merchantCode 商户编码
     * @return 商户信息
     */
    MerchantDTO getMerchantByCode(Long tenantId, String merchantCode);

    /**
     * 根据状态查询商户列表
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsByStatus(Long tenantId, String status);

    /**
     * 分页查询商户列表
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsPage(Long tenantId, String status, Integer pageNum, Integer pageSize);

    /**
     * 创建商户
     *
     * @param merchantDTO 商户信息
     * @return 创建的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO);

    /**
     * 更新商户
     *
     * @param merchantDTO 商户信息
     */
    void updateMerchant(MerchantDTO merchantDTO);

    /**
     * 删除商户
     *
     * @param merchantId 商户ID
     */
    void deleteMerchant(Long merchantId);

    /**
     * 更新商户状态
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
