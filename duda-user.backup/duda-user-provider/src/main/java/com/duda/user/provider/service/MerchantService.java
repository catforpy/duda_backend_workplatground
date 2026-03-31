package com.duda.user.provider.service;

import com.duda.user.dto.MerchantDTO;

import java.util.List;

/**
 * 商户Service接口（provider层）
 *
 * ⚠️ 重要：
 * 1. 这是provider层的业务逻辑接口
 * 2. 由MerchantServiceImpl实现真正的业务逻辑
 * 3. 被MerchantRpcImpl调用
 * 4. 负责数据库操作（通过Mapper）
 *
 * @author Claude
 * @date 2026-03-27
 */
public interface MerchantService {

    /**
     * 根据ID查询商户（带租户隔离）
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
     * 根据租户ID和商户编码查询（带租户隔离）
     *
     * @param tenantId 租户ID
     * @param merchantCode 商户编码
     * @return 商户信息
     */
    MerchantDTO getMerchantByCode(Long tenantId, String merchantCode);

    /**
     * 分页查询商户列表（带租户隔离）
     *
     * @param tenantId 租户ID
     * @param status 状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 商户列表
     */
    List<MerchantDTO> listMerchantsPage(Long tenantId, String status,
                                         Integer pageNum, Integer pageSize);

    /**
     * 创建商户（自动注入租户ID）
     *
     * @param merchantDTO 商户信息
     * @return 创建的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO);

    /**
     * 更新商户（带租户隔离和乐观锁）
     *
     * @param merchantDTO 商户信息
     */
    void updateMerchant(MerchantDTO merchantDTO);

    /**
     * 删除商户（带租户隔离）
     *
     * @param merchantId 商户ID
     */
    void deleteMerchant(Long merchantId);

    /**
     * 统计租户下的商户数量
     *
     * @param tenantId 租户ID
     * @return 商户数量
     */
    int countMerchantsByTenantId(Long tenantId);

    /**
     * 更新商户状态（带租户隔离和乐观锁）
     *
     * @param merchantId 商户ID
     * @param status 状态
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     */
    void updateMerchantStatus(Long merchantId, String status,
                              String auditStatus, String auditRemark);
}
