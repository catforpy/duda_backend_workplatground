package com.duda.user.rpc;

import com.duda.user.dto.merchant.MerchantDTO;

import java.util.List;

/**
 * 商户RPC接口
 *
 * ⚠️ 重要：
 * 1. 这是Dubbo服务接口定义
 * 2. provider层实现此接口（MerchantRpcImpl）并使用@DubboService注册到Nacos
 * 3. api层通过@DubboReference调用此接口
 * 4. 此接口是Dubbo服务的契约，所有层共用
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface IMerchantRpc {

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
     * 根据商户编码查询（带租户隔离）
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
