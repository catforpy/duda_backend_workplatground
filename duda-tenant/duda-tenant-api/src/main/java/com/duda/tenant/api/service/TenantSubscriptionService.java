package com.duda.tenant.api.service;

import com.duda.tenant.api.dto.TenantSubscriptionDTO;
import com.duda.tenant.api.dto.TenantSubscriptionLimitDTO;

import java.util.List;

/**
 * 租户订阅Service接口（API层）
 *
 * @author Claude Code
 * @since 2026-03-31
 */
public interface TenantSubscriptionService {

    /**
     * 根据ID查询订阅
     *
     * @param id 订阅ID
     * @return 订阅DTO
     */
    TenantSubscriptionDTO getById(Long id);

    /**
     * 根据订阅编号查询
     *
     * @param subscriptionCode 订阅编号
     * @return 订阅DTO
     */
    TenantSubscriptionDTO getBySubscriptionCode(String subscriptionCode);

    /**
     * 查询租户的所有订阅
     *
     * @param tenantId 租户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listByTenantId(Long tenantId);

    /**
     * 查询用户的所有订阅
     *
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listByUserId(Long userId);

    /**
     * 查询生效中的订阅
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 生效中的订阅
     */
    TenantSubscriptionDTO getActiveSubscription(Long tenantId, Long userId);

    /**
     * 创建订阅
     *
     * @param subscriptionDTO 订阅DTO
     * @return 创建的订阅
     */
    TenantSubscriptionDTO create(TenantSubscriptionDTO subscriptionDTO);

    /**
     * 更新订阅
     *
     * @param subscriptionDTO 订阅DTO
     * @return 更新后的订阅
     */
    TenantSubscriptionDTO update(TenantSubscriptionDTO subscriptionDTO);

    /**
     * 取消订阅
     *
     * @param id 订阅ID
     * @param cancelReason 取消原因
     * @param cancelBy 取消操作人
     * @return 是否成功
     */
    Boolean cancel(Long id, String cancelReason, Long cancelBy);

    /**
     * 暂停订阅
     *
     * @param id 订阅ID
     * @return 是否成功
     */
    Boolean suspend(Long id);

    /**
     * 激活订阅
     *
     * @param id 订阅ID
     * @return 是否成功
     */
    Boolean activate(Long id);

    /**
     * 续费订阅
     *
     * @param id 订阅ID
     * @param months 续费月数
     * @return 是否成功
     */
    Boolean renew(Long id, Integer months);

    /**
     * 查询订阅的所有限制条件
     *
     * @param subscriptionId 订阅ID
     * @return 限制条件列表
     */
    List<TenantSubscriptionLimitDTO> listLimits(Long subscriptionId);

    /**
     * 查询即将到期的订阅（7天内）
     *
     * @return 订阅列表
     */
    List<TenantSubscriptionDTO> listExpiringSoon();
}
